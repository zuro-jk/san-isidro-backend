package com.sanisidro.restaurante.features.customers.service;

import com.sanisidro.restaurante.core.exceptions.InvalidReservationException;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.model.Reservation;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import com.sanisidro.restaurante.features.customers.repository.ReservationRepository;
import com.sanisidro.restaurante.features.restaurant.model.TableEntity;
import com.sanisidro.restaurante.features.restaurant.repository.TableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TableRepository tableRepository;

    private LoyaltyService loyaltyService = mock(LoyaltyService.class);

    @InjectMocks
    private ReservationService reservationService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User user = User.builder()
                .id(10L)
                .firstName("Juan")
                .lastName("Pérez")
                .username("juanp")
                .email("juan@test.com")
                .password("1234")
                .build();

        customer = Customer.builder()
                .user(user)
                .points(0)
                .build();
    }

    @Test
    void createAutoWalkInReservation_ShouldAssignSmallestAvailableTable() {
        // given
        TableEntity table1 = new TableEntity();
        table1.setId(1L);
        table1.setName("Mesa 1");
        table1.setCapacity(2);
        table1.setOpenTime(LocalTime.of(9,0));
        table1.setCloseTime(LocalTime.of(23,0));
        table1.setReservationDurationMinutes(60);
        table1.setBufferBeforeMinutes(5);
        table1.setBufferAfterMinutes(5);

        TableEntity table2 = new TableEntity();
        table2.setId(2L);
        table2.setName("Mesa 2");
        table2.setCapacity(4);
        table2.setOpenTime(LocalTime.of(9,0));
        table2.setCloseTime(LocalTime.of(23,0));
        table2.setReservationDurationMinutes(60);
        table2.setBufferBeforeMinutes(5);
        table2.setBufferAfterMinutes(5);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(tableRepository.findAll()).thenReturn(List.of(table1, table2));
        when(reservationRepository.findByTable_IdAndReservationDate(anyLong(), any(LocalDate.class)))
                .thenReturn(List.of()); // no hay reservas
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation r = invocation.getArgument(0);
                    r.setId(99L);
                    return r;
                });

        // when
        var response = reservationService.createAutoWalkInReservation(1L, 3);

        // then
        assertNotNull(response);
        assertEquals(99L, response.getId());
        assertEquals(2L, response.getTableId());
        assertEquals("Juan Pérez", response.getCustomerName());
    }

    @Test
    void createAutoWalkInReservation_ShouldThrowExceptionWhenNoTablesAvailable() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(tableRepository.findAll()).thenReturn(List.of());

        assertThrows(InvalidReservationException.class,
                () -> reservationService.createAutoWalkInReservation(1L, 5));
    }
}
