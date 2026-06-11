package ar.edu.utn.dds.k3003.componentes;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Service
public class DonadoresYEntidadesClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public DonadoresYEntidadesClient(@Value("${DONADORESYENTIDADES_SERVICE_URL:http://localhost:8081}") String baseUrl) {
        this.baseUrl = baseUrl;
    }


    public List<NecesidadMaterialDTO> obtenerNecesidadesInsatisfechasDe(String productoSolicitadoID){
        try {
            // Armamos la URL exacta como dice el Swagger
            String url = baseUrl + "/necesidades?productoID=" + productoSolicitadoID;
            NecesidadMaterialDTO[] necesidades = restTemplate.getForObject(url, NecesidadMaterialDTO[].class);

            return necesidades != null
                    ? Arrays.asList(necesidades)
                    : Collections.emptyList();

        } catch (Exception e) {
            throw new RuntimeException("Error al consultar necesidades en DonadoresYEntidades", e);
        }
    }
    // POST necesidades/{necesidadID}/satisfaccion
    public NecesidadMaterialDTO satisfacerNecesidad(String necesidadID, Integer cantidad)
            throws NoSuchElementException {
        try{
            String url = baseUrl + "/necesidades/" + necesidadID + "/satisfaccion";

            Map<String, Integer> request = new HashMap<>();
            request.put("cantidad", cantidad);

            return restTemplate.postForObject(
                    url,
                    request,
                    NecesidadMaterialDTO.class
            );

        } catch (Exception e) {
            throw new RuntimeException("Error al satisfacer necesidad", e);
        }
    }
}
