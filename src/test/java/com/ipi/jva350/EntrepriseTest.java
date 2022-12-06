package com.ipi.jva350;

import com.ipi.jva350.model.Entreprise;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

public class EntrepriseTest {

    @Test
    void testEstDansPlage(){

        Entreprise entreprise = new Entreprise();
        LocalDate dateTest = LocalDate.parse("2022-12-05");
        LocalDate dateDebut = LocalDate.parse("2022-12-01");
        LocalDate dateFin = LocalDate.parse("2022-12-30");

        Assertions.assertTrue(entreprise.estDansPlage(dateTest, dateDebut, dateFin));

    }

    @ParameterizedTest()
    @CsvSource({
            "'2022-11-01', true",
            "'2022-01-10', false"
    })
    void testEstJourFerier(String dateTest, Boolean result){

        Entreprise entreprise = new Entreprise();

        Assertions.assertEquals(entreprise.estJourFerie(LocalDate.parse(dateTest)), result);

    }

    @ParameterizedTest()
    @CsvSource({
            "'2022-03-01', 0.8666666666666666",
            "'2022-02-01', 0.8"
    })
    void testProportionPondereeDuMois(String dateTest, Double expected){

        Entreprise entreprise = new Entreprise();

        Assertions.assertEquals(entreprise.proportionPondereeDuMois(LocalDate.parse(dateTest)), expected);



    }

    @ParameterizedTest()
    @CsvSource({
            "'2022-03-01', '2021-06-01'",
            "'2033-07-01', '0007-06-01'"
    })
    void testGetPremierJourAnneeDeConges(String dateTest, String expected){

        Entreprise entreprise = new Entreprise();

        //System.out.println(entreprise.getPremierJourAnneeDeConges(LocalDate.parse(dateTest)));
        Assertions.assertEquals(entreprise.getPremierJourAnneeDeConges(LocalDate.parse(dateTest)), LocalDate.parse(expected));

    }
}
