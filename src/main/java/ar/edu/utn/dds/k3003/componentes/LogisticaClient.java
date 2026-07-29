package ar.edu.utn.dds.k3003.componentes;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionMensajeDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.worker.model.NecesidadLogistica;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class LogisticaClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public LogisticaClient(@Value("https://logistica-tk85.onrender.com") String baseUrl) {

        System.out.println("LOGISTICA URL = " + baseUrl);

        this.baseUrl = baseUrl;
    }

    // POST /stock
    public void guardarEnStock(DonacionMensajeDTO donacion) {
        try {
            String url = baseUrl + "/stock";

            restTemplate.postForObject(
                    url,
                    donacion,
                    Void.class
            );

            System.out.println("Donación guardada en stock correctamente.");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al guardar donación en stock en Logística",
                    e
            );
        }
    }

    // POST /asignaciones
    public Integer asignarProductoAEntidad(NecesidadMaterialDTO necesidad) {
        try {
            String url = baseUrl + "/asignaciones";

            ResponseEntity<Integer> response = restTemplate.postForEntity(url, necesidad, Integer.class);

            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            // Leemos el mensaje original ("No hay stock disponible...") que mandó Logística
            String mensajeDeLogistica = e.getResponseBodyAsString();
            throw new NoSuchElementException(mensajeDeLogistica);

        }catch (Exception e) {
            throw new RuntimeException("Error de comunicación al gestionar la donación en Logística", e);
        }
    }
}

