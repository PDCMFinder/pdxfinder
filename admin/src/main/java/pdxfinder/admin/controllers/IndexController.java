package pdxfinder.admin.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by abayomi on 27/06/2017.
 */

@Controller
public class IndexController {

    @RequestMapping("/index")
    public String index() {
        System.out.println("Here we are ");
        return "index";
    }

}
