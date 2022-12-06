package com.ipi.jva350.model;

import com.ipi.jva350.repository.SalarieAideADomicileRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class SalarieAideAdomicileRepositoryIntegrationTest {

    @Autowired
    SalarieAideADomicileRepository salaireAideADomicileRep;
    @Test
    public void FindByNomTest(){
        // Given
        // When
        EntityNotFoundException e = Assertions.assertThrows(EntityNotFoundException.class, () ->
                salaireAideADomicileRep.findByNom("test")
        );
            // Then Exception
        Assertions.assertEquals(e.getMessage(), "Aide test non trouvé !");

    }
}
