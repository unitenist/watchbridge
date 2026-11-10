package com.sasa.watchbridge.ancs

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

class AncsParserTest {

    private fun attr(out: ByteArrayOutputStream, id: Int, value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        out.write(id)
        out.writeU16LE(bytes.size)
        out.write(bytes)
    }

    private fun notificationResponse(uid: Int): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(CommandId.GET_NOTIFICATION_ATTRIBUTES)
        out.writeU32LE(uid)
        attr(out, NotificationAttribute.APP_IDENTIFIER, "net.whatsapp.WhatsApp")
        attr(out, NotificationAttribute.TITLE, "Ahmet")
        attr(out, NotificationAttribute.SUBTITLE, "")
        attr(out, NotificationAttribute.MESSAGE, "Toplantı saat kaçta? 🙂")
        attr(out, NotificationAttribute.DATE, "20261105T230900")
        attr(out, NotificationAttribute.POSITIVE_ACTION_LABEL, "")
        attr(out, NotificationAttribute.NEGATIVE_ACTION_LABEL, "Temizle")
        return out.toByteArray()
    }

    @Test
    fun parsesNotificationSource() {
        val ev = NotificationSourceParser.parse(byteArrayOf(0, 0x18, 4, 1, 0x2A, 0, 0, 0))!!
        assertEquals(EventId.ADDED, ev.eventId)
        assertEquals(Category.SOCIAL, ev.category)
        assertEquals(42, ev.uid)
        assertTrue(ev.hasPositiveAction)
        assertTrue(ev.hasNegativeAction)
    }

    @Test
    fun rejectsShortNotificationSource() {
        assertNull(NotificationSourceParser.parse(byteArrayOf(0, 0, 0)))
    }

    @Test
    fun assemblesFragmentedNotificationAttributes() {
        val full = notificationResponse(0x01020304)
        val assembler = DataSourceAssembler()
        assembler.begin(CommandId.GET_NOTIFICATION_ATTRIBUTES, AncsCommands.NOTIFICATION_ATTRIBUTES.size)

        // 20 baytlık parçalar halinde besle (varsayılan MTU)
        var result: DataSourceResponse? = null
        full.toList().chunked(20).forEachIndexed { i, chunk ->
            val r = assembler.feed(chunk.toByteArray())
            if (i < (full.size - 1) / 20) assertNull("Erken tamamlandı (parça $i)", r)
            if (r != null) result = r
        }

        val n = IosNotification.from(result!!)!!
        assertEquals(0x01020304, n.uid)
        assertEquals("net.whatsapp.WhatsApp", n.appId)
        assertEquals("Ahmet", n.title)
        assertEquals("Toplantı saat kaçta? 🙂", n.message)
        assertEquals("Temizle", n.negativeLabel)
    }

    @Test
    fun parsesAppAttributes() {
        val out = ByteArrayOutputStream()
        out.write(CommandId.GET_APP_ATTRIBUTES)
        out.write("com.apple.MobileSMS".toByteArray())
        out.write(0)
        attr(out, AppAttribute.DISPLAY_NAME, "Mesajlar")

        val assembler = DataSourceAssembler()
        assembler.begin(CommandId.GET_APP_ATTRIBUTES, 1)
        val r = assembler.feed(out.toByteArray())
        assertNotNull(r)
        assertEquals("com.apple.MobileSMS", r!!.appId)
        assertEquals("Mesajlar", r.attributes[AppAttribute.DISPLAY_NAME])
    }

    @Test
    fun encodesCommandsLittleEndian() {
        assertArrayEquals(
            byteArrayOf(2, 4, 3, 2, 1, 1),
            AncsCommands.performNotificationAction(0x01020304, ActionId.NEGATIVE)
        )
        val get = AncsCommands.getNotificationAttributes(7)
        assertEquals(18, get.size) // varsayılan MTU'ya (20 bayt) sığmalı
        assertEquals(0, get[0].toInt())
        assertEquals(7, get[1].toInt())
    }
}
