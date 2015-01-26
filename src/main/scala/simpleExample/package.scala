package object simpleExample {
	type Event = CQRS.DomainEvent
	type Command = CQRS.Command
	type Date = java.util.Date
	type GUID = java.util.UUID
	
	import com.novus.salat._
	implicit val ctx = new Context {
        val name = "Custom Context"
        // some overrides or custom behavior
    }
	
	import org.slf4j.Logger
	val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)
}

