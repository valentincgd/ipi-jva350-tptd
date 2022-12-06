package com.ipi.jva350.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

class SalarieAideADomicileTest {



    @Test
    void testALegalementDroitADesCongesPayesDefault(){
        SalarieAideADomicile aide = new SalarieAideADomicile();

        boolean res = aide.aLegalementDroitADesCongesPayes();

        Assertions.assertFalse(res);
    }

    @Test
    void testALegalementDroitADesCongesPayesWithWorkingDays(){
        SalarieAideADomicile aide = new SalarieAideADomicile();

        aide.setJoursTravaillesAnneeNMoins1(100);

        boolean res = aide.aLegalementDroitADesCongesPayes();


        Assertions.assertTrue(res);
    }


    @ParameterizedTest()
    @CsvSource({
            "'2022-12-12','2022-12-24',12",
            "'2022-12-01', '2022-12-31',27"
    })
    void testCalculeJoursDeCongeDecomptesPourPlage(String dateDebutStr, String dateFinStr,int expected){

        LocalDate dateDebut = LocalDate.parse(dateDebutStr);
        LocalDate dateFin = LocalDate.parse(dateFinStr);

        SalarieAideADomicile aide = new SalarieAideADomicile();


        LinkedHashSet<LocalDate> jourDeCongesDecomptes = aide.calculeJoursDeCongeDecomptesPourPlage(dateDebut,dateFin);
        Assertions.assertEquals(jourDeCongesDecomptes.size(),expected);
    }








}