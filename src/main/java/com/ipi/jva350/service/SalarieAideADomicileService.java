package com.ipi.jva350.service;

import com.ipi.jva350.exception.SalarieException;
import com.ipi.jva350.model.Entreprise;
import com.ipi.jva350.model.SalarieAideADomicile;
import com.ipi.jva350.repository.SalarieAideADomicileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Service
public class SalarieAideADomicileService {

    @Autowired
    private SalarieAideADomicileRepository salarieAideADomicileRepository;

    public SalarieAideADomicileService() {
        // TODO document why this constructor is empty
    }

    /**
     * Créée un nouveau salarié en base de données.
     *
     * @param salarieAideADomicile à créer
     * @throws SalarieException si son nom est déjà pris ou si l'id est fourni
     */
    public void creerSalarieAideADomicile(SalarieAideADomicile salarieAideADomicile)
            throws SalarieException, EntityExistsException {
        SalarieAideADomicile existant = salarieAideADomicileRepository.findByNom(salarieAideADomicile.getNom());
        if (existant != null) {
            throw new SalarieException("Un salarié existe déjà avec le nom " + existant.getNom());
        }
        if (salarieAideADomicile.getId() != null) {
            throw new SalarieException("L'id ne doit pas être fourni car il est généré");
        }
        salarieAideADomicileRepository.save(salarieAideADomicile);
    }

    /**
     * Calcule la limite maximale de congés prenable autorisée selon les règles de
     * l'entreprise, à savoir :
     * - de base, les congés acquis en année N-1 dans la proportion selon
     * l'avancement dans l'année,
     * pondéré avec poids plus gros sur juillet et août (20 vs 8),
     * - si la moyenne actuelle des congés pris diffère de 20% de la précédente
     * limite,
     * bonus ou malus de 20% de la différence pour aider à équilibrer la moyenne
     * actuelle des congés pris
     * - marge supplémentaire de 10% du nombre de mois jusqu'à celui du dernier jour
     * de congé
     * - bonus de 1 par année d'ancienneté jusqu'à 10
     * Utilisé par ajouteMois(). NB. ajouteMois() a déjà vérifié que le congé est
     * dans l'année en cours.
     *
     * @param moisEnCours du salarieAideADomicile
     * @param congesPayesAcquisAnneeNMoins1 du salarieAideADomicile
     * @parma moisDebutContrat du salarieAideADomicile
     * @param premierJourDeConge demandé
     * @param dernierJourDeConge demandé
     * @return arrondi à l'entier le plus proche
     */
    public long calculeLimiteEntrepriseCongesPermis(LocalDate moisEnCours, double congesPayesAcquisAnneeNMoins1,
                                                    LocalDate moisDebutContrat,
                                                    LocalDate premierJourDeConge, LocalDate dernierJourDeConge) {
        // proportion selon l'avancement dans l'année, pondérée avec poids plus gros sur
        // juillet et août (20 vs 8) :
        double proportionPondereeDuConge = Math.max(Entreprise.proportionPondereeDuMois(premierJourDeConge),
                Entreprise.proportionPondereeDuMois(dernierJourDeConge));
        double limiteConges = proportionPondereeDuConge * congesPayesAcquisAnneeNMoins1;

        // moyenne annuelle des congés pris :
        Double partCongesPrisTotauxAnneeNMoins1 = salarieAideADomicileRepository.partCongesPrisTotauxAnneeNMoins1();

        // si la moyenne actuelle des congés pris diffère de 20% de la la proportion
        // selon l'avancement dans l'année
        // pondérée avec poids plus gros sur juillet et août (20 vs 8),
        // bonus ou malus de 20% de la différence pour aider à équilibrer la moyenne
        // actuelle des congés pris :
        double proportionMoisEnCours = ((premierJourDeConge.getMonthValue()
                - Entreprise.getPremierJourAnneeDeConges(moisEnCours).getMonthValue()) % 12) / 12d;
        double proportionTotauxEnRetardSurLAnnee = proportionMoisEnCours - partCongesPrisTotauxAnneeNMoins1;
        limiteConges += proportionTotauxEnRetardSurLAnnee * 0.2 * congesPayesAcquisAnneeNMoins1;

        // marge supplémentaire de 10% du nombre de mois jusqu'à celui du dernier jour
        // de congé
        int distanceMois = (dernierJourDeConge.getMonthValue() - moisEnCours.getMonthValue()) % 12;
        limiteConges += limiteConges * 0.1 * distanceMois / 12;

        // année ancienneté : bonus jusqu'à 10
        int anciennete = moisEnCours.getYear() - moisDebutContrat.getYear();
        limiteConges += Math.min(anciennete, 10);

        // arrondi pour éviter les miettes de calcul en Double :
        BigDecimal limiteCongesBd = new BigDecimal(Double.toString(limiteConges));
        limiteCongesBd = limiteCongesBd.setScale(3, RoundingMode.HALF_UP);
        return Math.round(limiteCongesBd.doubleValue());
    }

    /**
     * Calcule les jours de congés à décompter, et si valide (voir plus bas) les
     * décompte au salarié
     * et le sauve en base de données
     *
     * @param salarieAideADomicile TODO nom ?
     * @param jourDebut
     * @param jourFin              peut être dans l'année suivante mais uniquement
     *                             son premier jour
     * @throws SalarieException si pas de jour décompté, ou avant le mois en cours,
     *                          ou dans l'année suivante
     *                          (hors l'exception du premier jour pour résoudre le
     *                          cas d'un samedi), ou la nouvelle totalité
     *                          des jours de congé pris décomptés dépasse le nombre
     *                          acquis en N-1 ou la limite de l'entreprise
     */
    public void ajouteConge(SalarieAideADomicile salarieAideADomicile, LocalDate jourDebut, LocalDate jourFin)
            throws SalarieException {
        if (!salarieAideADomicile.aLegalementDroitADesCongesPayes()) {
            throw new SalarieException("N'a pas légalement droit à des congés payés !");
        }

        LinkedHashSet<LocalDate> joursDecomptes = salarieAideADomicile
                .calculeJoursDeCongeDecomptesPourPlage(jourDebut, jourFin);

        if (joursDecomptes.isEmpty()) {
            throw new SalarieException("Pas besoin de congés !");
        }

        // on vérifie que le congé demandé est dans les mois restants de l'année de
        // congés en cours du salarié :
        if (joursDecomptes.stream().findFirst().get()
                .isBefore(salarieAideADomicile.getMoisEnCours())) {
            throw new SalarieException("Pas possible de prendre de congé avant le mois en cours !");
        }
        LinkedHashSet<LocalDate> congesPayesPrisDecomptesAnneeN = new LinkedHashSet<>(joursDecomptes.stream()
                .filter(d -> !d.isAfter(LocalDate.of(Entreprise.getPremierJourAnneeDeConges(
                        salarieAideADomicile.getMoisEnCours()).getYear() + 1, 5, 31)))
                .collect(Collectors.toList()));
        int nbCongesPayesPrisDecomptesAnneeN = congesPayesPrisDecomptesAnneeN.size();
        if (joursDecomptes.size() > nbCongesPayesPrisDecomptesAnneeN + 1) {
            // NB. 1 jour dans la nouvelle année est toujours toléré, pour résoudre le cas
            // d'un congé devant se finir un
            // samedi le premier jour de la nouvelle année de congés...
            throw new SalarieException(
                    "Pas possible de prendre de congé dans l'année de congés suivante (hors le premier jour)");
        }

        if (nbCongesPayesPrisDecomptesAnneeN > salarieAideADomicile.getCongesPayesRestantAnneeNMoins1()) {
            throw new SalarieException("Conges Payes Pris Decomptes (" + nbCongesPayesPrisDecomptesAnneeN
                    + ") dépassent les congés acquis en année N-1 : "
                    + salarieAideADomicile.getCongesPayesRestantAnneeNMoins1());
        }

        double limiteEntreprise = this.calculeLimiteEntrepriseCongesPermis(
                salarieAideADomicile.getMoisEnCours(),
                salarieAideADomicile.getCongesPayesAcquisAnneeNMoins1(),
                salarieAideADomicile.getMoisDebutContrat(),
                jourDebut, jourFin);
        if (nbCongesPayesPrisDecomptesAnneeN < limiteEntreprise) {
            throw new SalarieException("Conges Payes Pris Decomptes (" + nbCongesPayesPrisDecomptesAnneeN
                    + ") dépassent la limite des règles de l'entreprise : " + limiteEntreprise);
        }

        salarieAideADomicile.getCongesPayesPris().addAll(joursDecomptes);
        salarieAideADomicile.setCongesPayesPrisAnneeNMoins1(nbCongesPayesPrisDecomptesAnneeN);

        salarieAideADomicileRepository.save(salarieAideADomicile);
    }

    /**
     * Clôture le mois donné :
     * (pas forcément en cours, par exemple en cas de retard, vacances de
     * l'entreprise)
     * Met à jour les jours travaillés et congés payés restants de l'année N,
     * décompte ceux de l'année N-1
     * on déduit un jour de congé entier pour chaque absence. Par exemple lors des
     * vacances, pour savoir combien de jours de congés payés sont consommés, même
     * si ladite absence dure seulement une demi-journée.
     * Si dernier mois de l'année, clôture aussi l'année
     *
     * @param salarieAideADomicile TODO nom ?
     * @param joursTravailles
     */
    public void clotureMois(SalarieAideADomicile salarieAideADomicile, double joursTravailles) {
        salarieAideADomicile
                .setJoursTravaillesAnneeN(salarieAideADomicile.getJoursTravaillesAnneeN() + joursTravailles);

        salarieAideADomicile.setCongesPayesAcquisAnneeN(salarieAideADomicile.getCongesPayesAcquisAnneeN()
                + SalarieAideADomicile.congesPayesAcquisParMois);

        salarieAideADomicile.setMoisEnCours(salarieAideADomicile.getMoisEnCours().plusMonths(1));

        if (salarieAideADomicile.getMoisEnCours().getMonth().getValue() == 6) {
            clotureAnnee(salarieAideADomicile);
        }

        salarieAideADomicileRepository.save(salarieAideADomicile);
    }


    void clotureAnnee(SalarieAideADomicile salarieAideADomicile) {
        salarieAideADomicile.setJoursTravaillesAnneeNMoins1(salarieAideADomicile.getJoursTravaillesAnneeN());
        salarieAideADomicile.setCongesPayesAcquisAnneeNMoins1(salarieAideADomicile.getCongesPayesAcquisAnneeN());
        salarieAideADomicile.setCongesPayesPrisAnneeNMoins1(0);
        salarieAideADomicile.setJoursTravaillesAnneeN(0);
        salarieAideADomicile.setCongesPayesAcquisAnneeN(0);

        salarieAideADomicile.setCongesPayesPris(new LinkedHashSet<>(salarieAideADomicile.getCongesPayesPris().stream()
                .filter(d -> d.isAfter(LocalDate.of(Entreprise.getPremierJourAnneeDeConges(
                        salarieAideADomicile.getMoisEnCours()).getYear(), 5, 31)))
                .collect(Collectors.toList())));

        salarieAideADomicileRepository.save(salarieAideADomicile);
    }

}