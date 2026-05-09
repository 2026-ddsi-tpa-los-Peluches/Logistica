package ar.edu.utn.dds.k3003.model;
import ar.edu.utn.dds.k3003.catedra.ClassFinder;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
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


@ExtendWith(MockitoExtension.class)
@EnabledIf("ar.edu.utn.dds.k3003.catedra.logistica.LogisticaTest#condicion")


public class TestPropiosLogistica {
    FachadaLogistica instancia;
    @Mock FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
    @Mock FachadaDonaciones fachadaDonaciones;

    DepositoDTO depositoEjemplo;
    NecesidadDeEntidadDTO necesidadDeEjemplo;
    PaqueteDTO paqueteEjemplo;

    @SneakyThrows
    @BeforeEach
    void setUp() {

        var clazz = ClassFinder.findClass();
        instancia = (FachadaLogistica) clazz.getDeclaredConstructor().newInstance();
        instancia.setAlgoritmoMM();

        instancia.setFachadaDonadoresYEntidades(fachadaDonadoresYEntidades);
        instancia.setFachadaDonaciones(fachadaDonaciones);

        depositoEjemplo = new DepositoDTO(null, "deposito1", "direccion1", 1000, null);
        necesidadDeEjemplo =
                new NecesidadDeEntidadDTO(null, "entidad1", 5, "descripcion1", 10, "producto1");
        paqueteEjemplo = new PaqueteDTO("paquete1", "donacion1", "producto1", 10);
    }



    @Test
    void testPropioAgregarDeposito() {
        // Capacidad maxima mayor a 50 cuando la capacidad maxima asignada fue 100
        DepositoDTO dto = new DepositoDTO(null, "Depo", "Dir", 100, null);
        DepositoDTO retorno = instancia.agregarDeposito(dto);

        Assertions.assertNotNull(retorno.id());
        Assertions.assertEquals(retorno.nombre(), dto.nombre());
        Assertions.assertTrue(retorno.capacidadMaxima() > 50);
    }

    @Test
    void testPropioBuscarDepositoPorID() {
        DepositoDTO creado = instancia.agregarDeposito(depositoEjemplo);

        DepositoDTO encontrado = instancia.buscarDepositoPorID(creado.id());

        Assertions.assertEquals(creado.id(), encontrado.id());
    }

    @Test
    void testPropioBuscarAsignacionPorPaqueteID() {

        try {
            instancia.buscarAsignacionPorPaqueteID("paqueteIdQueNoExisteEnMiSistema");
        } catch (NoSuchElementException e){
            String errorString = e.toString();
            Assertions.assertEquals("java.util.NoSuchElementException: No existe asignación", errorString);
        }
    }

    @Test
    void testPropioGestionarDonacion() {
        // Verifica que al gestionar una donación, si existen necesidades para el producto,
        // se seleccione una y se llame a satisfacerNecesidad con la cantidad donada.

        instancia.agregarDeposito(depositoEjemplo);
        DepositoDTO depo = instancia.agregarDeposito(depositoEjemplo);

        NecesidadMaterialDTO necesidad = new NecesidadMaterialDTO(
                "74-3-188", "com-cba-23", 3, "necesito vasos", 34, "prod74"
        );

        org.mockito.Mockito
                .when(fachadaDonadoresYEntidades.obtenerNecesidadesInsatisfechasDe("prod74"))
                .thenReturn(List.of(necesidad));

        instancia.gestionarDonacion(depo.id(), "74_5_16-04-26", "prod74", 5);

            Mockito.verify(fachadaDonadoresYEntidades)
                .satisfacerNecesidad("74-3-188", 5);
    }

    @Test
    void testPropioEjecutarMatchmaking() {
        // Verifica que el algoritmo de matchmaking seleccione la necesidad
        // con mayor cantidadObjetivo (la más sub-atendida).
        PaqueteDTO paquete = new PaqueteDTO("17-18-49", "17_18_17-08-25", "prod17", 18);

        NecesidadDeEntidadDTO n1 = new NecesidadDeEntidadDTO("17-20-188", "esc-men-50", 8, "sillas para alumnos", 20, "prod17");
        NecesidadDeEntidadDTO n2 = new NecesidadDeEntidadDTO("17-40-189", "hos-cor-67", 1, "sillas para pacientes", 40, "prod17");

        AsignacionDTO asignacion = instancia.ejecutarMatchmaking(paquete, List.of(n1, n2));

        Assertions.assertEquals("17-40-189", asignacion.necesidadID()); // el de mayor cantidad
    }

    @Test
    void testPropioReportarEntrega() {

        // gestionar donacion -> recibe una donacion y trata de darsela a una entidad segun el algoritmo
        // ejecutarMM -> recibe un paquete y una lista de entidades que necesitan un producto x y
        // devuelve una asignacion de la entidad seleccionada
        try {
            instancia.reportarEntrega(paqueteEjemplo);
        } catch (NoSuchElementException e){
            String errorString = e.toString();
            Assertions.assertEquals("java.util.NoSuchElementException: No existe asignación para ese paquete", errorString);
        }
    }
}
