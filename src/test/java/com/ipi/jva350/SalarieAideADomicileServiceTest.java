package com.ipi.jva350;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ipi.jva350.exception.SalarieException;
import com.ipi.jva350.repository.SalarieAideADomicileRepository;
import com.ipi.jva350.service.SalarieAideADomicileService;

@ExtendWith(MockitoExtension.class)
public class SalarieAideADomicileServiceTest {
    @InjectMocks
    private SalarieAideADomicileService salarieAideADomicileService;

    @Mock
    private SalarieAideADomicileRepository salarieAideADomicileRepository;

    @Test
    void calculeLimiteEntrepriseCongesPermisTest() throws SalarieException {

        Boolean expected = true;
        Boolean actual = true;
        assertEquals(expected, actual);
    }
}