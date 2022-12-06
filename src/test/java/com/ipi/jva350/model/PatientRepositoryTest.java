package com.ipi.jva350.model;

import com.ipi.jva350.model.SalarieAideADomicile;
import com.ipi.jva350.repository.SalarieAideADomicileRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class PatientRepositoryTest {

    @Autowired
    SalarieAideADomicileRepository salarieAideADomicileRepository;

    @Test
    public void testFindByNom(){

        SalarieAideADomicile aide = new SalarieAideADomicile();
        aide.setNom("bayet");
        salarieAideADomicileRepository.save(aide);
        Assertions.assertEquals(salarieAideADomicileRepository.findByNom("bayet"), aide);

    }

}