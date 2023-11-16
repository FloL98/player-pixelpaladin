package thkoeln.dungeon.planet.application


import org.springframework.web.bind.annotation.GetMapping
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model

@Controller
class PlanetHtmlController {
    private val logger = LoggerFactory.getLogger(PlanetHtmlController::class.java)
    @GetMapping("/map")
    fun main(model: Model): String {
        logger.info("called /map")
        model.addAttribute("message", "jdsajdlskajdlaksj")
        return "map" //view
    }
}