package ar.edu.utn.dds.k3003.model;
import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.ClassFinder;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@EnabledIf("ar.edu.utn.dds.k3003.catedra.logistica.LogisticaTest#condicion")


public class TestPropiosLogistica {


    Fachada instancia;

    @Mock
    FachadaDonadoresYEntidades fachadaDonadoresYEntidades;

    @Mock
    FachadaDonaciones fachadaDonaciones;

    DepositoDTO depositoEjemplo;
    NecesidadMaterialDTO necesidadEjemplo;
    PaqueteDTO paqueteEjemplo;

    @SneakyThrows
    @BeforeEach
    void setUp() {

        instancia = new Fachada();

        instancia.setFachadaDonadoresYEntidades(fachadaDonadoresYEntidades);
        instancia.setFachadaDonaciones(fachadaDonaciones);

        depositoEjemplo =
                new DepositoDTO(
                        null,
                        null,
                        "Deposito Central",
                        "Direccion 123",
                        100,
                        null
                );

        necesidadEjemplo =
                new NecesidadMaterialDTO(
                        "necesidad1",
                        "entidad1",
                        10,
                        "descripcion",
                        5,
                        "producto1",
                        TipoNecesidadMaterialEnum.EXTRAORDINARIA
                );

        paqueteEjemplo =
                new PaqueteDTO(
                        "paquete1",
                        "donacion1",
                        "producto1",
                        10
                );
    }

    @Test
    void testObtenerDepositos() {

        instancia.agregarDeposito(depositoEjemplo);

        List<DepositoDTO> depositos = instancia.obtenerDepositos();

        Assertions.assertNotNull(depositos);
        Assertions.assertEquals(1, depositos.size());
        Assertions.assertEquals("Deposito Central", depositos.getFirst().nombre());
    }

    @Test
    void testBorrarDepositoPorID() {

        DepositoDTO deposito = instancia.agregarDeposito(depositoEjemplo);

        DepositoDTO eliminado = instancia.borrarDepositoPorID(deposito.id());

        Assertions.assertNotNull(eliminado);
        Assertions.assertEquals(deposito.id(), eliminado.id());

        Assertions.assertThrows(
                NoSuchElementException.class,
                () -> instancia.buscarDepositoPorID(deposito.id())
        );
    }

    @Test
    void testBuscarAsignacionPorID() {

        DepositoDTO deposito = instancia.agregarDeposito(depositoEjemplo);

        instancia.setAlgoritmoMM(
                deposito.id(),
                TipoAlgoritmoEnum.SUB_ATENDIDOS
        );

        AsignacionDTO asignacion =
                instancia.ejecutarMatchmaking(
                        deposito.id(),
                        paqueteEjemplo,
                        List.of(necesidadEjemplo)
                );

        AsignacionDTO encontrada =
                instancia.buscarAsignacionPorID(asignacion.id());

        Assertions.assertNotNull(encontrada);
        Assertions.assertEquals(asignacion.id(), encontrada.id());
    }

    @Test
    void testBuscarAsignacionPorIDFallido() {

        Assertions.assertThrows(
                NoSuchElementException.class,
                () -> instancia.buscarAsignacionPorID("id-inexistente")
        );
    }

    @Test
    void testBuscarPaquetePorID() {

        when(fachadaDonadoresYEntidades.obtenerNecesidadesInsatisfechasDe("producto1"))
                .thenReturn(List.of(necesidadEjemplo));

        DepositoDTO deposito = instancia.agregarDeposito(depositoEjemplo);

        instancia.setAlgoritmoMM(
                deposito.id(),
                TipoAlgoritmoEnum.SUB_ATENDIDOS
        );

        instancia.gestionarDonacion(
                deposito.id(),
                "donacion1",
                "producto1",
                10
        );

        PaqueteDTO paquete = instancia.buscarPaquetePorID("1");

        Assertions.assertNotNull(paquete);
        Assertions.assertEquals("producto1", paquete.producto());
    }

    @Test
    void testBuscarPaquetePorIDFallido() {

        Assertions.assertThrows(
                NoSuchElementException.class,
                () -> instancia.buscarPaquetePorID("inexistente")
        );
    }

    @Test
    void testGestionarDonacionSinNecesidades() {

        when(fachadaDonadoresYEntidades.obtenerNecesidadesInsatisfechasDe("producto1"))
                .thenReturn(List.of());

        DepositoDTO deposito = instancia.agregarDeposito(depositoEjemplo);

        instancia.setAlgoritmoMM(
                deposito.id(),
                TipoAlgoritmoEnum.SUB_ATENDIDOS
        );

        Assertions.assertThrows(
                NoSuchElementException.class,
                () -> instancia.gestionarDonacion(
                        deposito.id(),
                        "donacion1",
                        "producto1",
                        10
                )
        );
    }

    @Test
    void testGestionarDonacionRecurrenteNoAceptaParcialidad() {

        NecesidadMaterialDTO necesidadRecurrente =
                new NecesidadMaterialDTO(
                        "necesidad1",
                        "entidad1",
                        5,
                        "Necesita alimentos semanalmente",
                        100,
                        "producto1",
                        TipoNecesidadMaterialEnum.RECURRENTE
                );

        when(fachadaDonadoresYEntidades
                .obtenerNecesidadesInsatisfechasDe("producto1"))
                .thenReturn(List.of(necesidadRecurrente));

        DepositoDTO deposito =
                instancia.agregarDeposito(depositoEjemplo);

        instancia.setAlgoritmoMM(
                deposito.id(),
                TipoAlgoritmoEnum.SUB_ATENDIDOS
        );

        Assertions.assertThrows(
                NoSuchElementException.class,
                () -> instancia.gestionarDonacion(
                        deposito.id(),
                        "donacion1",
                        "producto1",
                        50 // menor a los 100 requeridos
                )
        );
    }

    @Test
    void testGestionarDonacionRecurrenteAceptaCantidadCompleta() {

        NecesidadMaterialDTO necesidadRecurrente =
                new NecesidadMaterialDTO(
                        "necesidad1",
                        "entidad1",
                        5,
                        "Necesita alimentos semanalmente",
                        100,
                        "producto1",
                        TipoNecesidadMaterialEnum.RECURRENTE
                );

        when(fachadaDonadoresYEntidades
                .obtenerNecesidadesInsatisfechasDe("producto1"))
                .thenReturn(List.of(necesidadRecurrente));

        DepositoDTO deposito =
                instancia.agregarDeposito(depositoEjemplo);

        instancia.setAlgoritmoMM(
                deposito.id(),
                TipoAlgoritmoEnum.SUB_ATENDIDOS
        );

        DepositoDTO resultado =
                instancia.gestionarDonacion(
                        deposito.id(),
                        "donacion1",
                        "producto1",
                        100
                );

        Assertions.assertNotNull(resultado);

        verify(fachadaDonadoresYEntidades, times(1))
                .obtenerNecesidadesInsatisfechasDe("producto1");
    }

    @Test
    void testGestionarDonacionRecurrenteAceptaCantidadMayor() {

        NecesidadMaterialDTO necesidadRecurrente =
                new NecesidadMaterialDTO(
                        "necesidad1",
                        "entidad1",
                        5,
                        "Necesita alimentos semanalmente",
                        100,
                        "producto1",
                        TipoNecesidadMaterialEnum.RECURRENTE
                );

        when(fachadaDonadoresYEntidades
                .obtenerNecesidadesInsatisfechasDe("producto1"))
                .thenReturn(List.of(necesidadRecurrente));

        DepositoDTO deposito =
                instancia.agregarDeposito(depositoEjemplo);

        instancia.setAlgoritmoMM(
                deposito.id(),
                TipoAlgoritmoEnum.SUB_ATENDIDOS
        );

        DepositoDTO resultado =
                instancia.gestionarDonacion(
                        deposito.id(),
                        "donacion1",
                        "producto1",
                        150
                );

        Assertions.assertNotNull(resultado);
    }

    @Test
    void testSetAlgoritmoMM() {

        DepositoDTO deposito =
                instancia.agregarDeposito(
                        new DepositoDTO(null, null, "Deposito", "Direccion", 100, null)
                );

        instancia.setAlgoritmoMM(
                deposito.id(),
                TipoAlgoritmoEnum.SUB_ATENDIDOS
        );

        DepositoDTO actualizado =
                instancia.buscarDepositoPorID(deposito.id());

        Assertions.assertEquals(
                TipoAlgoritmoEnum.SUB_ATENDIDOS,
                actualizado.algoritmo()
        );
    }

    @Test
    void testEjecutarMatchmakingSinNecesidades() {

        DepositoDTO deposito = instancia.agregarDeposito(depositoEjemplo);

        instancia.setAlgoritmoMM(
                deposito.id(),
                TipoAlgoritmoEnum.SUB_ATENDIDOS
        );

        Assertions.assertThrows(
                NoSuchElementException.class,
                () -> instancia.ejecutarMatchmaking(
                        deposito.id(),
                        paqueteEjemplo,
                        List.of()
                )
        );
    }

    @Test
    void testAsignacionSeCreaEnEstadoAsignada() {

        DepositoDTO deposito =
                instancia.agregarDeposito(depositoEjemplo);

        instancia.setAlgoritmoMM(
                deposito.id(),
                TipoAlgoritmoEnum.SUB_ATENDIDOS
        );

        AsignacionDTO asignacion =
                instancia.ejecutarMatchmaking(
                        deposito.id(),
                        paqueteEjemplo,
                        List.of(necesidadEjemplo)
                );

        Assertions.assertEquals(
                EstadoAsginacionEnum.ASIGNADA,
                asignacion.estado()
        );
    }

    @Test
    void testReportarEntregaSinAsignacion() {

        when(fachadaDonaciones.cambiarEstadoDeDonacion(
                paqueteEjemplo.donacionID(),
                EstadoDonacionEnum.ACEPTADA
        )).thenReturn(null);

        Assertions.assertThrows(
                NoSuchElementException.class,
                () -> instancia.reportarEntrega(paqueteEjemplo)
        );

        verify(fachadaDonaciones, times(1))
                .cambiarEstadoDeDonacion(
                        paqueteEjemplo.donacionID(),
                        EstadoDonacionEnum.ACEPTADA
                );
    }
}