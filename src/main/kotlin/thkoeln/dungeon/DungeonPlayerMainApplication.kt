package thkoeln.dungeon



import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.SpringApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer


//@SpringBootApplication
open class DungeonPlayerMainApplication : SpringBootServletInitializer()


fun main(args: Array<String>) {
    SpringApplication.run(DungeonPlayerMainApplication::class.java, *args)
}





