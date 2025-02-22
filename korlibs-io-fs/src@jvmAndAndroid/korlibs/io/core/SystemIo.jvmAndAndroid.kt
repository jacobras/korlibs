package korlibs.io.core

import kotlinx.coroutines.*
import java.io.*

actual val defaultSyncSystemIo: SyncSystemIo = JvmSyncSystemIo
actual val defaultSystemIo: SystemIo = defaultSyncSystemIo.toAsync(Dispatchers.IO)

object JvmSyncSystemIo : SyncSystemIo() {
    override val fileSeparatorChar: Char get() = File.separatorChar
    override val pathSeparatorChar: Char get() = File.pathSeparatorChar

    override fun mkdir(path: String) = File(path).mkdir()
    override fun rmdir(path: String) = File(path).takeIf { it.isDirectory }?.delete() == true
    override fun unlink(path: String) = File(path).takeIf { !it.isDirectory }?.delete() == true
    override fun listdir(path: String): Sequence<String> = (File(path).list() ?: emptyArray<String>()).asSequence()
    override fun stat(path: String): FileSystemIoStat? {
        val file = File(path).takeIf { it.exists() } ?: return null
        return FileSystemIoStat(
            name = file.name,
            size = file.length(),
            timeLastModification = file.lastModified(),
            isDirectory = file.isDirectory,
        )
    }
    override fun open(path: String, write: Boolean): SyncFileSystemIo? {
        val file = File(path).takeIf { it.exists() } ?: return null
        val s = RandomAccessFile(file, if (write) "rw" else "r")
        return object : SyncFileSystemIo() {
            override fun getLength(): Long = s.length()
            override fun setLength(value: Long) = s.setLength(value)
            override fun getPosition(): Long = s.filePointer
            override fun setPosition(value: Long) = s.seek(value)
            override fun read(data: ByteArray, offset: Int, size: Int): Int = s.read(data, offset, size)
            override fun write(data: ByteArray, offset: Int, size: Int): Unit = s.write(data, offset, size)
            override fun close() = s.close()
        }
    }
}
