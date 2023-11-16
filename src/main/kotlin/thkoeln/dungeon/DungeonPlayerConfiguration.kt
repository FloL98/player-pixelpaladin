package thkoeln.dungeon


import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.client.RestTemplate


@Configuration
@EnableRabbit
@EnableAutoConfiguration
@EntityScan("thkoeln.dungeon.*")
@ComponentScan("thkoeln.dungeon.*")
open class DungeonPlayerConfiguration {

    @Autowired
    private var restTemplateBuilder: RestTemplateBuilder? = null

    @Autowired
    private val environment: Environment? = null

    /**
     * Needed for configuration of the RabbitMQ connection
     * @return
     */
    @Bean
    open fun connectionFactory(): CachingConnectionFactory {
        val connectionFactory = CachingConnectionFactory()
        val username = environment!!.getProperty("queue.username")
        val password = environment.getProperty("queue.password")
        connectionFactory.username = username
        connectionFactory.setPassword(password)
        connectionFactory.host = environment.getProperty("queue.host") as String
        connectionFactory.port = 5672
        return connectionFactory

    }

    /*@Bean
    open fun restTemplateBuilder(): RestTemplateBuilder? {
        return RestTemplateBuilder()
    }*/


    private val QUEUE = "player-queue-PixelPaladin"
    private val EXCHANGE = "player-queue-PixelPaladin"
    private val ROUTING_KEY = "#"
    /*@Bean
    open fun queue(): Queue{
        return Queue(QUEUE)
    }

    @Bean open fun binding(queue: Queue, topicExchange: TopicExchange): Binding{
        return BindingBuilder
            .bind(queue)
            .to(topicExchange)
            .with(ROUTING_KEY)
    }

    @Bean
    open fun exchange(): TopicExchange{
        return TopicExchange(EXCHANGE)
    }*/


    /**
     * @return
     */
    @Bean
    open fun restTemplate(): RestTemplate {
        return restTemplateBuilder!!.build()
    }




}




