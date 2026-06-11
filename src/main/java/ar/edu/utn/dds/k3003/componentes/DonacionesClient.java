package ar.edu.utn.dds.k3003.componentes;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DonacionesClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public DonacionesClient(@Value("${DONACIONES_SERVICE_URL:http://localhost:8081}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    // Corresponde al endpoint Patch "/donaciones/{id}/estado"
    public DonacionDTO cambiarEstadoDeDonacion(String donacionID, EstadoDonacionEnum estado)
            throws NoSuchElementException {

                try{
                    String url = baseUrl + "/necesidades/" + donacionID + "/estado";
                    return restTemplate.patchForObject(
                            url,
                            estado,
                            DonacionDTO.class
                    );
                } catch (Exception e) {
                    throw new NoSuchElementException("Error al cambiar estado de la donación", e);

                }
    }
}