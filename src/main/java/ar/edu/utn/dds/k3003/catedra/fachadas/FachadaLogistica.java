package ar.edu.utn.dds.k3003.catedra.fachadas;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import ar.edu.utn.dds.k3003.model.Paquete;

import java.util.List;
import java.util.NoSuchElementException;

public interface FachadaLogistica {

  DepositoDTO agregarDeposito(DepositoDTO deposito);

  DepositoDTO buscarDepositoPorID(Integer depositoID) throws NoSuchElementException;

  AsignacionDTO buscarAsignacionPorPaqueteID(Integer paqueteID) throws NoSuchElementException;

  AsignacionDTO gestionarDonacion(
          Integer depositoID, String donacionID, String  productoID, Integer cantidad)
      throws NoSuchElementException;

  void setAlgoritmoMM(Integer depositoID, TipoAlgoritmoEnum tipoAlgoritmo);

  void reportarEntrega(PaqueteDTO paqueteDTO);
}
