package CQRS

import eventstore._
import eventstore.persistence.MongoPersistenceEngine
import com.mongodb.casbah.Imports._
import akka.actor._

case class DeactivateInventoryItem(inventoryItemId: GUID, originalVersion: Int) extends Command
case class CreateInventoryItem(inventoryItemId: GUID, name: String) extends Command
case class RenameInventoryItem(inventoryItemId: GUID, newName: String, originalVersion: Int) extends Command
case class CheckInItemsToInventory(inventoryItemId: GUID, count: Int, originalVersion: Int) extends Command
case class RemoveItemsFromInventory(inventoryItemId: GUID, count: Int, originalVersion: Int) extends Command

case class InventoryItemCreated(id: GUID, name: String) extends DomainEvent
case class InventoryItemRenamed(id: GUID, newName: String) extends DomainEvent
case class InventoryItemDeactivated(id: GUID) extends DomainEvent
case class ItemsCheckedInToInventory(id: GUID, count: Int) extends DomainEvent
case class ItemsRemovedFromInventory(id: GUID, count: Int) extends DomainEvent

case class InventoryItemState(id: GUID, activated: Boolean)

class InventoryItem extends AggregateRoot {
  var activated: Boolean = false
  var id: GUID = java.util.UUID.randomUUID()

  def this(id_ : GUID, name_ : String) = {
    this()
    applyChange(InventoryItemCreated(id_, name_))
  }

  def handle: PartialFunction[DomainEvent, Unit] = {
    case e: InventoryItemCreated => handle(e)
    case e: InventoryItemDeactivated => handle(e)
  }

  def handle(e: InventoryItemCreated) = {
    id = e.id
    activated = true
  }

  def handle(e: InventoryItemDeactivated) = {
    activated = false
  }

  def changeName(newName: String) = {
    if (newName.isEmpty) throw new Exception("newName")
    applyChange(InventoryItemRenamed(id, newName))
  }

  def remove(count: Int) {
    if (count <= 0) throw new Exception("cant remove negative count from inventory");
    applyChange(ItemsRemovedFromInventory(id, count));
  }

  def checkIn(count: Int) {
    if (count <= 0) throw new Exception("must have a count greater than 0 to add to inventory");
    applyChange(ItemsCheckedInToInventory(id, count));
  }

  def deactivate() {
    if (!activated) throw new Exception("already deactivated");
    applyChange(InventoryItemDeactivated(id));
  }

}

class InventoryCommandHandlers(repository: IRepository) extends CommandHandler {
  def receive: PartialFunction[Command, Unit] = {
    case c: CreateInventoryItem => handle(c)
    case c: DeactivateInventoryItem => handle(c)
    case c: RemoveItemsFromInventory => handle(c)
    case c: CheckInItemsToInventory => handle(c)
    case c: RenameInventoryItem => handle(c)
  }

  def handle(c: CreateInventoryItem) = {
    val item = new InventoryItem(c.inventoryItemId, c.name)
    repository.save(item, -1)
  }

  def handle(c: DeactivateInventoryItem) = {
    val item = repository.getById(c.inventoryItemId, new InventoryItem)
    item.deactivate
    repository.save(item, c.originalVersion)
  }

  def handle(c: RemoveItemsFromInventory) = {
    val item = repository.getById(c.inventoryItemId, new InventoryItem)
    item.remove(c.count)
    repository.save(item, c.originalVersion)
  }
  def handle(c: CheckInItemsToInventory) = {
    val item = repository.getById(c.inventoryItemId, new InventoryItem)
    item.checkIn(c.count)
    repository.save(item, c.originalVersion)
  }
  def handle(c: RenameInventoryItem) = {
    val item = repository.getById(c.inventoryItemId, new InventoryItem)
    item.changeName(c.newName)
    repository.save(item, c.originalVersion)
  }
}

case class InventoryItemListDto(id: GUID, name: String)
case class InventoryItemDetailsDto(id: GUID, name: String, currentCount: Int, version: Int)

object BullShitDatabase {
  var details = Map[GUID, InventoryItemDetailsDto]()
  var list = List[InventoryItemListDto]()
}

object ReadModelFacade /* : IReadModelFacade*/ {
  def getInventoryItems(): List[InventoryItemListDto] =
    {
      BullShitDatabase.list
    }

  def getInventoryItemDetails(id: Guid): Option[InventoryItemDetailsDto] =
    {
      return BullShitDatabase.details.get(id)
    }
}

object InventoryListView extends EventStreamReceiver //: Handles<InventoryItemCreated>, Handles<InventoryItemRenamed>, Handles<InventoryItemDeactivated>
{
  def handle(ce: CommitedEvent): Unit = {
    ce.event match {
      case a: InventoryItemRenamed => handle(a, ce.streamRevision)
      case a: InventoryItemCreated => handle(a, ce.streamRevision)
      //case a: ItemsRemovedFromInventory => handle(a, ce.streamRevision)
      //case a: ItemsCheckedInToInventory => handle(a, ce.streamRevision)
      case a: InventoryItemDeactivated => handle(a, ce.streamRevision)
      case _ => ()
    }
  }

  def handle(message: InventoryItemCreated, version: Int) = {
    BullShitDatabase.list = BullShitDatabase.list.+:(InventoryItemListDto(message.id, message.name))
  }

  def handle(message: InventoryItemRenamed, version: Int) = {
    BullShitDatabase.list = BullShitDatabase.list.map { x => if (x.id == message.id) x.copy(name = message.newName) else x }
  }

  def handle(message: InventoryItemDeactivated, version: Int) = {
    BullShitDatabase.list = BullShitDatabase.list.filter(x => x.id != message.id)
  }
}
object InventoryItemDetailView extends Logging with EventStreamReceiver {

  def handle(ce: CommitedEvent): Unit = {

    log.info(s"${ce.streamId} , ${ce.streamRevision} handled")
    ce.event match {
      case a: InventoryItemRenamed => handle(a, ce.streamRevision)
      case a: InventoryItemCreated => handle(a, ce.streamRevision)
      case a: ItemsRemovedFromInventory => handle(a, ce.streamRevision)
      case a: ItemsCheckedInToInventory => handle(a, ce.streamRevision)
      case a: InventoryItemDeactivated => handle(a, ce.streamRevision)
    }
  }

  def handle(message: InventoryItemCreated, version: Int) = {
    BullShitDatabase.details = BullShitDatabase.details + (message.id -> InventoryItemDetailsDto(message.id, message.name, 0, version))
  }
  def handle(message: InventoryItemRenamed, version: Int) =
    {
      val d = GetDetailsItem(message.id);
      val newd = d.copy(name = message.newName, version = version)
      BullShitDatabase.details = BullShitDatabase.details.updated(message.id, newd)
    }

  private def GetDetailsItem(id: Guid): InventoryItemDetailsDto =
    {
      val d = BullShitDatabase.details.get(id)
      if (!d.isDefined) {
        throw new Exception("did not find the original inventory this shouldnt happen");
      }
      d.get
    }

  def handle(message: ItemsRemovedFromInventory, version: Int) = {
    val d = GetDetailsItem(message.id)
    val newd = d.copy(currentCount = d.currentCount - message.count, version = version)
    BullShitDatabase.details = BullShitDatabase.details.updated(message.id, newd)

  }

  def handle(message: ItemsCheckedInToInventory, version: Int) = {
    val d = GetDetailsItem(message.id);
    val newd = d.copy(currentCount = d.currentCount + message.count, version = version)
    BullShitDatabase.details = BullShitDatabase.details.updated(message.id, newd)

  }

  def handle(message: InventoryItemDeactivated, version: Int) = {
    BullShitDatabase.details = BullShitDatabase.details - message.id;
  }
}
