package com.ipi.jva350;

import java.time.LocalDate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.ipi.jva350.model.Entreprise;

public class EntrepriseTest {

    @ParameterizedTest
    @CsvSource({
            "'2022-11-01', true",
            "'2022-08-15', true",
            "'2022-05-01', true",
            "'2022-12-25', true",
            "'2022-01-01', true",
            "'2022-02-11', false",
            "'2022-10-23', false",
            "'2022-03-27', false",
            "'2022-09-09', false",

    })
    void testEstJourFerie(LocalDate jour, Boolean expected) {

        Boolean res = Entreprise.estJourFerie(jour);

        Assertions.assertThat(res).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "'2022-11-03', '2022-11-02', '2022-11-04', true",
            "'2022-11-02', '2022-11-02', '2022-11-04', true",
            "'2022-11-04', '2022-11-02', '2022-11-04', true",
            "'2022-11-01', '2022-11-02', '2022-11-04', false",
            "'2022-11-05', '2022-11-02', '2022-11-04', false",

    })
    void testEstDansPlage(LocalDate d, LocalDate debutDate, LocalDate finDate, Boolean expected) {

        Boolean res = Entreprise.estDansPlage(d, debutDate, finDate);

        Assertions.assertThat(res).isEqualTo(expected);
    }
}