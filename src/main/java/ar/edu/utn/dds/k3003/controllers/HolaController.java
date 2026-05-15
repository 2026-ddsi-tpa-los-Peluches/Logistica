package ar.edu.utn.dds.k3003.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolaController {

    @GetMapping("/hola")
    public String hola(){
        return "hola mundo";
    }
}
