package info.syachiku.testing;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static info.syachiku.testing.Inquiry.Status.*;

class CaseDesignTest {
    @Test void T01_title() { assertEquals(false, TitleRule.accepts(null), "T01"); }
    @Test void T02_title() { assertEquals(false, TitleRule.accepts(""), "T02"); }
    @Test void T03_title() { assertEquals(true, TitleRule.accepts("a"), "T03"); }
    @Test void T04_title() { assertEquals(true, TitleRule.accepts("aa"), "T04"); }
    @Test void T05_title() { assertEquals(true, TitleRule.accepts("a".repeat(99)), "T05"); }
    @Test void T06_title() { assertEquals(true, TitleRule.accepts("a".repeat(100)), "T06"); }
    @Test void T07_title() { assertEquals(false, TitleRule.accepts("a".repeat(101)), "T07"); }
    @Test void T08_title() { assertEquals(false, TitleRule.accepts(" "), "T08"); }
    @Test void T09_title() { assertEquals(false, TitleRule.accepts("\u3000"), "T09"); }
    @Test void T10_title() { assertEquals(true, TitleRule.accepts("😀".repeat(100)), "T10"); }
    @Test void T11_title() { assertEquals(false, TitleRule.accepts("😀".repeat(101)), "T11"); }
    @Test void T12_title() { assertEquals(true, TitleRule.accepts("a".repeat(99) + " "), "T12"); }
    @Test void T13_title() { assertEquals(false, TitleRule.accepts("a".repeat(100) + " "), "T13"); }
    @Test void A01_start() {
        Inquiry inquiry = new Inquiry(10, 20L, RECEIVED);
        assertEquals(true, inquiry.start(20), "A01: acceptance");
        assertEquals(IN_PROGRESS, inquiry.status(), "A01: status");
        assertEquals(10L, inquiry.creatorId(), "A01: creator unchanged");
        assertEquals(Long.valueOf(20), inquiry.assigneeId(), "A01: assignee unchanged");
    }
    @Test void A02_start() {
        Inquiry inquiry = new Inquiry(10, 20L, RECEIVED);
        assertEquals(false, inquiry.start(10), "A02: acceptance");
        assertEquals(RECEIVED, inquiry.status(), "A02: status");
        assertEquals(10L, inquiry.creatorId(), "A02: creator unchanged");
        assertEquals(Long.valueOf(20), inquiry.assigneeId(), "A02: assignee unchanged");
    }
    @Test void A03_start() {
        Inquiry inquiry = new Inquiry(10, 20L, RECEIVED);
        assertEquals(false, inquiry.start(30), "A03: acceptance");
        assertEquals(RECEIVED, inquiry.status(), "A03: status");
        assertEquals(10L, inquiry.creatorId(), "A03: creator unchanged");
        assertEquals(Long.valueOf(20), inquiry.assigneeId(), "A03: assignee unchanged");
    }
    @Test void A04_start() {
        Inquiry inquiry = new Inquiry(10, null, RECEIVED);
        assertEquals(false, inquiry.start(20), "A04: acceptance");
        assertEquals(RECEIVED, inquiry.status(), "A04: status");
        assertEquals(10L, inquiry.creatorId(), "A04: creator unchanged");
        assertEquals(null, inquiry.assigneeId(), "A04: assignee unchanged");
    }
    @Test void A05_start() {
        Inquiry inquiry = new Inquiry(10, 20L, DRAFT);
        assertEquals(false, inquiry.start(20), "A05: acceptance");
        assertEquals(DRAFT, inquiry.status(), "A05: status");
        assertEquals(10L, inquiry.creatorId(), "A05: creator unchanged");
        assertEquals(Long.valueOf(20), inquiry.assigneeId(), "A05: assignee unchanged");
    }
    @Test void A06_start() {
        Inquiry inquiry = new Inquiry(10, 20L, IN_PROGRESS);
        assertEquals(false, inquiry.start(20), "A06: acceptance");
        assertEquals(IN_PROGRESS, inquiry.status(), "A06: status");
        assertEquals(10L, inquiry.creatorId(), "A06: creator unchanged");
        assertEquals(Long.valueOf(20), inquiry.assigneeId(), "A06: assignee unchanged");
    }
    @Test void A07_start() {
        Inquiry inquiry = new Inquiry(10, 20L, COMPLETED);
        assertEquals(false, inquiry.start(20), "A07: acceptance");
        assertEquals(COMPLETED, inquiry.status(), "A07: status");
        assertEquals(10L, inquiry.creatorId(), "A07: creator unchanged");
        assertEquals(Long.valueOf(20), inquiry.assigneeId(), "A07: assignee unchanged");
    }
}
