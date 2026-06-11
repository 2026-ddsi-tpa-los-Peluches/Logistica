package ar.edu.utn.dds.k3003.repositories.paquetes;

import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.model.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;



public interface PaquetesRepository extends JpaRepository<Paquete, Integer> {
}



//@Repository
//public interface PaquetesRepository {
//    Optional<Paquete> findById(String id);
//
//    Paquete save(Paquete paquete);
//
//    Paquete deleteById(String id);
//
//}