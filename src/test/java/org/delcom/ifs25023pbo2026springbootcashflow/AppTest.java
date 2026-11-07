package org.delcom.ifs25023pbo2026springbootcashflow;

import org.delcom.ifs25023pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25023pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25023pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

/**
 * Smoke test: memastikan wiring bean Clean Architecture benar
 * dan App.main mendelegasikan ke SpringApplication.run.
 */
@SpringBootTest
class AppTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsAndBeansAreWired() {
        assertNotNull(context.getBean(ITransactionRepository.class));
        assertNotNull(context.getBean(FinanceUseCase.class));
        assertNotNull(context.getBean(FinancePresenter.class));
    }

    @Test
    void balanceIsConsistentAcrossLayers() {
        // Gunakan instance baru agar tidak bergantung pada state bean singleton.
        FinanceUseCase useCase = new FinanceUseCase(
                new org.delcom.ifs25023pbo2026springbootcashflow.adapter.repository.TransactionRepository());
        FinancePresenter presenter = context.getBean(FinancePresenter.class);

        assertDoesNotThrow(() -> {
            useCase.addIncome("Gaji", 5_000_000.0);
            useCase.addExpense("Makan", 25_000.0);
        });

        assertEquals(4_975_000.0, useCase.getBalance(), 0.001);
        assertEquals("Saldo: Rp 4,975,000.00", presenter.showBalance(useCase.getBalance()));
    }

    @Test
    void mainDelegatesToSpringApplicationRun() {
        try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
            String[] args = new String[0];
            assertDoesNotThrow(() -> App.main(args));
            spring.verify(() -> SpringApplication.run(eq(App.class), any(String[].class)));
        }
    }
}
