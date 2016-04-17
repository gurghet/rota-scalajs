import rx.lang.scala.Observable
import shared.WorkerId

import scala.collection.immutable.IndexedSeq
import scala.collection.mutable

/**
  * Created by gurghet on 05.04.16.
  */
class Rota(nDays: Int, team: Set[WorkerId]) {
  private val shiftsPerDay = 3
  private val maxGlobalTeamSize = 5
  private val r = scala.util.Random
  type MutableRota = mutable.LinkedHashMap[(Int, Int), mutable.Set[WorkerId]]
  type Rota = Map[(Int, Int), Set[WorkerId]]
  type ImmutableRota = collection.immutable.Map[(Int, Int), collection.immutable.Set[WorkerId]]

  private var lastAdd: Option[((WorkerId, mutable.Set[WorkerId]))] = Option.empty
  private var lastDel: Option[((WorkerId, mutable.Set[WorkerId]))] = Option.empty


  // all the shifts have an implicit day and order
  // for example the with 3 shifts per day the 4th
  // shift is the 1st shift of the second day
  private val rota = mutable.LinkedHashMap.empty[(Int, Int), mutable.Set[WorkerId]]
  private var shiftProperties = List.empty[collection.immutable.Set[String]]
  private var workerPreferences = collection.immutable.Map.empty[WorkerId, List[Int]]

  def allShifts: IndexedSeq[(Int, Int)] = (1 to nDays)
    .flatMap(day => (1 to shiftsPerDay).map(shift => (day, shift)))

  /**
    * Write an initial random solution
    */
  def init(point: Option[Rota] = Option.empty) {

    if (point.isEmpty) {
      // calculations are 1-indexed
      allShifts
        .foreach { case (day, shift) =>
          val currentShiftTeam = mutable.HashSet.empty[WorkerId]
          val availableWorkers = team.toBuffer
          for (i <- 1 to r.nextInt(math.min(maxGlobalTeamSize, team.size)) + 1) {
            val randomWorker: Int = r.nextInt(availableWorkers.size)
            currentShiftTeam.add(availableWorkers.remove(randomWorker))
          }
          rota += (day, shift) -> currentShiftTeam
        }
    } else {
      point.get.foreach{case ((day, shift), equipe) =>
        rota += (day, shift) -> (mutable.Set() ++ equipe)
      }
    }
  }

  def get(): ImmutableRota = {
    allShifts
      .map{ case (day, shift) =>
        (day, shift) -> rota(day, shift).toSet
      }.toMap[(Int, Int), collection.immutable.Set[WorkerId]]
  }

  def randomShift = (r.nextInt(nDays) + 1, r.nextInt(shiftsPerDay) + 1)

  def drawRandomWorkerFromRandomShift(): Option[WorkerId] = {
    var shift = randomShift
    var equipe = rota(shift)
    var trial = 1; val maxTrials = 5
    while (equipe.isEmpty && trial <= maxTrials) {
      trial += 1
      shift = randomShift
      equipe = rota(shift)
    }
    if (equipe.isEmpty) {
      lastDel = Option.empty
      Option.empty[WorkerId]
    } else {
      val teamSize = equipe.size
      val pickedWorker = equipe.iterator.drop(r.nextInt(teamSize)).next
      equipe.remove(pickedWorker)
      //print(s" $shift->${pickedWorker.id} ")
      lastDel = Option((pickedWorker, equipe))
      Option(pickedWorker)
    }
  }

  def unDrawRandomWorkerFromRandomShift() {
    if (lastDel.isDefined) {
      val shift = lastDel.get._2
      shift += lastDel.get._1
    }
  }

  def addWorkerToRandomShift(worker: WorkerId) {
    var shift = randomShift
    var equipe = rota(shift)
    var trial = 1; val maxTrials = 5
    // if the worker is already present take another shift
    while (equipe.contains(worker) && trial <= maxTrials) {
      trial += 1
      shift = randomShift
      equipe = rota(shift)
    }
    if (!equipe.contains(worker)) {
      //print(s" ${worker.id}->$shift ")
      lastAdd = Option((worker, equipe))
      equipe += worker
    } else {
      lastAdd = Option.empty
    }

  }

  def unAddWorkerToRandomShift(): Unit = {
    if (lastAdd.isDefined) {
      val shift = lastAdd.get._2
      shift -= lastAdd.get._1
    }
  }

  def movingRandomWorkerSomewhereElse() {
    val maybeWorker = drawRandomWorkerFromRandomShift()

    if (maybeWorker.isDefined) {
      val worker = maybeWorker.get
      addWorkerToRandomShift(worker)
    } else {
      unDrawRandomWorkerFromRandomShift()
    }
    //println("moving")
  }

  def unMovingRandomWorkerSomewhereElse() {
    //println("undo moving")
    unAddWorkerToRandomShift()
    unDrawRandomWorkerFromRandomShift()
  }

  def addRandomWorkerToRandomShift() {
    //println("adding")
    val randomWorker: WorkerId = team.iterator.drop(r.nextInt(team.size)).next
    addWorkerToRandomShift(randomWorker)
  }

  def unAddRandomWorkerToRandomShift() {
    //println("undo adding")
    unAddWorkerToRandomShift()
  }

  def removeRandomWorkerFromRandomShift() {
    //println("removing")
    drawRandomWorkerFromRandomShift()
  }

  def unRemoveRandomWorkerFromRandomShift() {
    //println("undo removing")
    unDrawRandomWorkerFromRandomShift()
  }

  case class Move(f: () => Unit, `f⁻¹`: () => Unit)

  def pickNeighborhood(): Move = {
    val neighborhoods = Set(
      Move(removeRandomWorkerFromRandomShift, unRemoveRandomWorkerFromRandomShift),
      Move(addRandomWorkerToRandomShift, unAddRandomWorkerToRandomShift),
      Move(movingRandomWorkerSomewhereElse, unMovingRandomWorkerSomewhereElse)
    )
    neighborhoods.iterator.drop(r.nextInt(neighborhoods.size)).next
  }

  def setPreferencesFor(workerId: WorkerId, preferences: Map[(Int, Int), Int]) {
    val preferenceList = allShifts
      .map{ case (day, shift) =>
        preferences((day, shift))
      }.toList
    workerPreferences += (workerId -> preferenceList)
  }

  def setShiftProperties(properties: Map[(Int, Int), Set[String]]) {
    shiftProperties = properties.values.toList
  }

  def getPresenceListFor(workerId: WorkerId): List[Int] = {
    allShifts
      .map{ case (day, shift) =>
        if (rota((day, shift)) contains workerId) 1 else 0
      }.toList
  }

  def simplePresencesToLoss(presences: List[Int]): Int = {
    var retVal = 0
    Observable.from(presences)
      .sliding(3, 1)
      .map(_.sum)
      .flatten
      .map(sum => if (sum == 0 || sum == 1) 0 else sum * -2 + 2)
      .sum
      .subscribe {
        result => retVal = result
      }
    retVal
  }

  def gain(): Double = {
    val point = get
    var sum = 0D
    for (w <- team) {
      val preferences = workerPreferences.getOrElse(w, List.fill(nDays * shiftsPerDay)(0))
      val presences = getPresenceListFor(w)
      sum += preferences.zip(presences).map{case (preference, presence) => preference * presence}.sum
      sum += simplePresencesToLoss(presences)
    }

    // controllo delle proprietà locali
    val props = shiftProperties.zip(rota)
    for (s <- props) {
      val equipe = s._2._2
      val shift = s._2._1
      val props = s._1
      var idealEquipeSize = 1
      if (props.contains("mattino") && !props.contains("vacanza")) {
        idealEquipeSize = 4
      }
      if (props.contains("altroRep")) {
        idealEquipeSize = 0
      }
      sum -= math.pow(idealEquipeSize - equipe.size, 2)
    }
    // controllo in un intorno di 5 giorni
    // controllo proprietà globali

    sum
  }



  def go(maxMoves: Int = 1000): ImmutableRota = {
    var currentGain = gain
    for (i <- 1 to maxMoves) {
      val move = pickNeighborhood
      move.f()
      val newGain = gain
      if (newGain < currentGain) {
        //println(s"gain no better than $currentGain, will undo")
        move.`f⁻¹`()
      } else {
        currentGain = newGain
        //println(s"gain $currentGain->$newGain!")
      }
    }
    get
  }
}