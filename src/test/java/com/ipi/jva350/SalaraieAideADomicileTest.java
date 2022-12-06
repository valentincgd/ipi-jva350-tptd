package com.ipi.jva350;

import com.ipi.jva350.model.SalarieAideADomicile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.LinkedHashSet;

public class SalaraieAideADomicileTest {

    @ParameterizedTest(name = "numeroSecu {0} est valide : {1}")
    @CsvSource({
            "'2022-11-20', 10",
            "'2022-12-12', 0"
    })
    void testCheckBadNumeroSecu(String numeroSecu, int expected)
    {
        LocalDate dateDebut = LocalDate.parse(numeroSecu);
        LocalDate dateFin = LocalDate.parse("2022-11-30");

        SalarieAideADomicile aide = new SalarieAideADomicile();
        LinkedHashSet<LocalDate> jourDeCongesDecomptes = aide.calculeJoursDeCongeDecomptesPourPlage(dateDebut, dateFin);
        Assertions.assertEquals(jourDeCongesDecomptes.size(), expected);
    }

    @Test
    void testALegalementDroitADesCongesPayesDefault(){

        SalarieAideADomicile aide = new SalarieAideADomicile();
        boolean res = aide.aLegalementDroitADesCongesPayes();
        Assertions.assertFalse(res);

    }

    @Test
    void testALegalementDroitADesCongesPayes10(){

        SalarieAideADomicile aide = new SalarieAideADomicile();
        boolean res = aide.aLegalementDroitADesCongesPayes();
        Assertions.assertFalse(res);

    }

}