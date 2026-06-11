package ar.edu.utn.dds.k3003.repositories.paquetes;

import ar.edu.utn.dds.k3003.model.Paquete;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

//public class InMemoryPaquetesRepository implements PaquetesRepository {
//
//    private List<Paquete> paquetes;
//    private AtomicLong idSecuencial = new AtomicLong(1);
//
//    public InMemoryPaquetesRepository() {
//        this.paquetes = new ArrayList<>();
//    }
//
//    @Override
//    public Optional<Paquete> findById(String id) {
//        return this.paquetes.stream().filter(p -> p.getId().equals(id)).findFirst();
//    }
//
//    @Override
//    public Paquete save(Paquete paquete) {
//        paquete.setId(String.valueOf(idSecuencial.getAndIncrement()));
//
//        this.paquetes.add(paquete);
//        return paquete;
//    }
//
//    @Override
//    public Paquete deleteById(String id) {
//        val paquete = this.findById(id);
//        this.paquetes.remove(paquete.get());
//        return paquete.get();
//    }
//}