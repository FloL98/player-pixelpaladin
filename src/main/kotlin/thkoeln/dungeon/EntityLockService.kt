package thkoeln.dungeon

import kotlinx.coroutines.sync.Mutex
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class EntityLockService {

    var robotLocks: ConcurrentHashMap<UUID, Mutex> = ConcurrentHashMap()
    var planetLocks: ConcurrentHashMap<UUID, Mutex> = ConcurrentHashMap()

    var playerLock: Mutex = Mutex()
    var strategyLock: Mutex = Mutex()


    fun clearAllLocks(){
        robotLocks = ConcurrentHashMap()
        planetLocks = ConcurrentHashMap()
    }
}