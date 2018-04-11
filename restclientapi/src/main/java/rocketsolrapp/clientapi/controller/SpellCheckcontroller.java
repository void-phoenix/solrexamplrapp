package rocketsolrapp.clientapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rocketsolrapp.clientapi.service.SpellCheckService;

import java.util.List;

@RestController
@RequestMapping("/spellcheck")
@ResponseStatus(value = HttpStatus.OK)
public class SpellCheckcontroller {

    @Autowired
    SpellCheckService spellCheckService;

    @RequestMapping(method = RequestMethod.GET)
    public List<String> checkSpelling(@RequestParam String keywords) throws Exception{
        return spellCheckService.checkSpelling(keywords);
    }

}
