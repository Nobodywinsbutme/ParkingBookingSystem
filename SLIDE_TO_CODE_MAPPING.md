# Smart Parking Booking System — Slide-to-Code Mapping
**Presenter:** Pham Trung Kien | **Team:** 7-member team, International University | **Date:** 2026

---

## **SLIDE 1: Title & Introduction**

**Visual Content:**
- Project dashboard screenshot / Database ERD diagram
- Team photo (optional)

**Code References:**
- **Entry Point:** `be/src/main/java/com/app/Application.java:1-15`
  - Demonstrates Spring Boot 3.3 initialization
  - Sets UTC timezone for consistent booking timestamps

---

## **SLIDE 2: Architectural Decisions & Tech Stack**

**Visual Content:**
- Architecture diagram: HTTP Request → Controller → Service → Repository → Database

**Code References:**

### **Spring Boot Framework & Monolithic MVC:**
- **File:** `be/pom.xml` (Lines 1-96)
  - `spring-boot-starter-parent` v3.3.4 (Line 9)
  - `spring-boot-starter-web` (Line 26-28) — MVC controllers
  - `spring-boot-starter-security` (Line 30-32) — authentication
  - `spring-boot-starter-data-jpa` (Line 39-40) — Hibernate ORM
  - `spring-boot-starter-websocket` (Line 66-68) — real-time notifications
  - `stripe-java` v26.1.0 (Line 71-73) — Stripe payment SDK

### **Configuration & Bean Management:**
- **File:** `be/src/main/java/com/app/Application.java:1-15`
  - `@SpringBootApplication` with `@ConfigurationPropertiesScan`
  - Spring Boot 3.3.4 initialization
  
### **View Layer (JSP + Bootstrap 5):**
- **File:** `be/src/main/resources/application.yml:14-17`
  - Spring MVC view resolver configuration
  - JSP prefix: `/WEB-INF/views/`
  - JSP suffix: `.jsp`

---

## **SLIDE 3: Database Design & PDM Academic Integration**

**Visual Content:**
- ERD showing 5 tables: users, parking_area, parking_slot, booking, payment
- Constraint highlights: unique indexes, foreign keys

**Code References:**

### **Database Schema (Constraints & Integrity):**
- **File:** `be/src/main/resources/schema.sql` (Lines 1-77)
  
  **Table: users** (Lines 3-10)
  ```sql
  CREATE TABLE users (
    id VARCHAR(32) PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),
    role VARCHAR(20),  -- 'ADMIN' or 'USER'
    ...
  )
  ```
  
  **Table: parking_area** (Lines 12-21)
  ```sql
  CREATE TABLE parking_area (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255),
    city VARCHAR(255),
    ...
  )
  ```
  
  **Table: parking_slot** (Lines 23-35)
  ```sql
  CREATE TABLE parking_slot (
    id VARCHAR(32) PRIMARY KEY,
    parking_area_id VARCHAR(32) FOREIGN KEY,
    code VARCHAR(64),
    status ENUM('AVAILABLE', 'BOOKED', 'MAINTENANCE'),
    -- UNIQUE constraint ensures no duplicate slot codes per area
    CONSTRAINT unique_slot_per_area UNIQUE (parking_area_id, code)
  )
  ```
  
  **Table: booking** (Lines 37-59)
  ```sql
  CREATE TABLE booking (
    id VARCHAR(32) PRIMARY KEY,
    user_id, parking_slot_id, parking_area_id FOREIGN KEYS,
    start_at DATETIME(6), end_at DATETIME(6),
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'),
    amount_total INT,  -- in cents (minor units)
    -- Composite index for overlap detection
    KEY idx_booking_slot_time (parking_slot_id, start_at, end_at)
  )
  ```
  
  **Table: payment** (Lines 61-76)
  ```sql
  CREATE TABLE payment (
    id VARCHAR(32) PRIMARY KEY,
    booking_id VARCHAR(32) FOREIGN KEY,
    method VARCHAR(32),  -- 'CASH' or 'STRIPE'
    status VARCHAR(32),  -- 'PENDING', 'PAID', 'FAILED', 'REFUNDED'
    provider_txn_ref VARCHAR(255),  -- Stripe Session ID
    -- UNIQUE constraint: one payment per booking
    CONSTRAINT unique_booking_payment UNIQUE (booking_id)
  )
  ```

### **Advanced SQL for PDM (JOIN, EXISTS, GROUP BY):**
- **File:** `be/src/main/java/com/app/web/QueryDemoRestController.java` (Lines 1-77)
  
  **JOIN Query** (Lines 37-40):
  ```java
  @GetMapping("/bookings/join")
  public List<BookingJoinRow> bookingsJoin() {
    return bookingRepository.findAllWithUserSlotAreaJoin();
  }
  ```
  Reference: `BookingRepository.java:159-172`
  ```sql
  SELECT b.id, u.email, s.code, a.name, b.startAt, b.endAt
  FROM booking b
  JOIN users u ON b.user_id = u.id
  JOIN parking_slot s ON b.parking_slot_id = s.id
  JOIN parking_area a ON b.parking_area_id = a.id
  WHERE b.deleted_at IS NULL
  ```
  
  **EXISTS Query** (Lines 45-64):
  ```java
  @GetMapping("/bookings/overlap-check")
  public OverlapCheckDemoView overlapCheck(
    @RequestParam String slotId, @RequestParam String startAt, @RequestParam String endAt)
  ```
  Reference: `BookingRepository.java:190-206`
  ```sql
  SELECT EXISTS(
    SELECT 1 FROM booking b
    WHERE b.parking_slot_id = :slotId
      AND b.deleted_at IS NULL
      AND b.status IN ('PENDING', 'CONFIRMED')
      AND b.start_at < :endAt
      AND b.end_at > :startAt
  )
  ```
  
  **GROUP BY Query** (Lines 70-75):
  ```java
  @GetMapping("/bookings/stats")
  public BookingStatsDemoView stats() {
    return new BookingStatsDemoView(
      bookingRepository.countBookingsGroupByStatus(),
      bookingRepository.countBookingsGroupByParkingArea()
    );
  }
  ```
  Reference: `BookingRepository.java:211-235`
  ```sql
  SELECT b.status, COUNT(b) FROM booking b
  WHERE b.deleted_at IS NULL
  GROUP BY b.status
  
  SELECT b.parking_area_id, a.name, COUNT(b)
  FROM booking b JOIN parking_area a ON b.parking_area_id = a.id
  WHERE b.deleted_at IS NULL
  GROUP BY b.parking_area_id, a.name
  ```

---

## **SLIDE 4: Core Business Flow & Concurrency Control**

**Visual Content:**
- Flow diagram: Registration → Browse → Create Booking → Payment → Check-in/Check-out
- Highlight: SERIALIZABLE transaction isolation box

**Code References:**

### **The Problem: Double-Booking Race Condition**
Example scenario: User A and User B both try to book Slot #5 at 9:00 AM simultaneously.

### **Solution: SERIALIZABLE Transaction Isolation**

**File:** `be/src/main/java/com/app/service/BookingService.java:72-175`

```java
@Transactional(isolation = Isolation.SERIALIZABLE)  // LINE 72
public BookingDto createBooking(String userId, CreateBookingRequest request) {
    
    // Step 1: Validate input (Lines 74-87)
    Instant startAt = parseInstant(request.startAt(), "Invalid startAt/endAt");
    Instant endAt = parseInstant(request.endAt(), "Invalid startAt/endAt");
    if (!endAt.isAfter(startAt)) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ...);
    }
    
    // Step 2: Verify parking area exists (Lines 89-91)
    if (!parkingAreaRepository.findByIdAndDeletedAtIsNullAndActiveTrue(request.parkingAreaId()).isPresent()) {
        throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Parking area not found or inactive.");
    }
    
    // Step 3: Validate slot availability (Lines 93-106)
    ParkingSlotEntity slot = parkingSlotRepository.findById(request.parkingSlotId())
        .orElseThrow(...);
    if (slot.getStatus() != SlotStatus.AVAILABLE) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", 
            "Parking slot is not available for booking.");
    }
    
    // Step 4: Check for overlaps (CRITICAL — under SERIALIZABLE lock)
    List<BookingStatus> activeStatuses = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
    
    // Check if user has conflicting booking (Lines 109-111)
    if (bookingRepository.existsOverlappingBookingForUser(userId, startAt, endAt, activeStatuses)) {
        throw new BookingConflictException("You already have another booking that overlaps this time range.");
    }
    
    // Check if slot already booked for this time range (Lines 112-115)
    if (bookingRepository.existsOverlappingBooking(request.parkingSlotId(), startAt, endAt, activeStatuses)) {
        throw new BookingConflictException("This parking slot is already booked for an overlapping time range.");
    }
    
    // Step 5: Create booking entity (Lines 117-146)
    int amountSubtotal = calculateBookingAmount(startAt, endAt);  // $50 USD per hour
    BookingEntity booking = new BookingEntity();
    booking.setId(uuidLikeId());
    booking.setUserId(userId);
    booking.setParkingSlotId(request.parkingSlotId());
    booking.setStartAt(startAt);
    booking.setEndAt(endAt);
    booking.setStatus(requiresOnlineCheckout ? BookingStatus.PENDING : BookingStatus.CONFIRMED);
    booking.setAmountTotal(amountTotal);
    BookingEntity savedBooking = bookingRepository.save(booking);
    
    // Step 6: Create payment record (Lines 152-171)
    PaymentEntity payment = new PaymentEntity();
    payment.setId(uuidLikeId());
    payment.setBookingId(savedBooking.getId());
    payment.setMethod(storedMethod);  // STRIPE or CASH
    payment.setStatus(requiresOnlineCheckout ? PaymentEntity.PaymentStatus.PENDING : PaymentEntity.PaymentStatus.PAID);
    paymentRepository.save(payment);
    
    // Step 7: Broadcast real-time notification (Line 173)
    slotAvailabilityNotifier.notifyAreaChanged(request.parkingAreaId());
    
    return toDto(savedBooking);
}
```

### **Why SERIALIZABLE?**
- **Without SERIALIZABLE:** Two transactions could both execute the `existsOverlappingBooking()` query, get `false`, and both proceed to insert bookings → double-booking!
- **With SERIALIZABLE:** Transaction 1 acquires exclusive lock; Transaction 2 waits; Transaction 1 inserts booking; Transaction 2 re-evaluates and detects conflict → throws `BookingConflictException`.

### **Overlap Detection Query:**
**File:** `be/src/main/java/com/app/repository/BookingRepository.java:48-61`
```java
@Query("""
    select count(b) > 0 from BookingEntity b
     where b.parkingSlotId = :parkingSlotId
       and b.deletedAt is null
       and b.status in :activeStatuses
       and b.startAt < :endAt        // Interval overlap condition
       and b.endAt > :startAt        // (start1 < end2) AND (end1 > start2)
""")
boolean existsOverlappingBooking(
    @Param("parkingSlotId") String parkingSlotId,
    @Param("startAt") Instant startAt,
    @Param("endAt") Instant endAt,
    @Param("activeStatuses") List<BookingStatus> activeStatuses
);
```

### **Booking Cancellation & Check-in/Check-out:**

**Cancellation** (Lines 177-219):
```java
@Transactional
public CancelBookingResponse cancelBooking(String userId, String bookingId) {
    BookingEntity booking = bookingRepository.findByIdAndUserIdAndDeletedAtIsNull(bookingId, userId)...
    
    // Enforce 30-minute cutoff before booking start
    if (booking.getStatus() == BookingStatus.CONFIRMED) {
        Instant cutoff = booking.getStartAt().minus(30, ChronoUnit.MINUTES);
        if (now.isAfter(cutoff)) {
            throw new ApiException(..., "Confirmed booking can only be cancelled at least 30 minutes before start time");
        }
    }
    
    // Mark as cancelled + refund if payment was made
    bookingRepository.cancelBooking(bookingId, userId, BookingStatus.CANCELLED, ...);
    paymentRepository.refundSucceededOnlineByBooking(bookingId, now);
    slotAvailabilityNotifier.notifyAreaChanged(booking.getParkingAreaId());
}
```

**Check-in** (Lines 221-229):
```java
@Transactional
public CheckInResponse checkIn(String userId, String bookingId) {
    Instant now = Instant.now();
    int updated = bookingRepository.checkIn(bookingId, userId, now, BookingStatus.CONFIRMED);
    // Can only check in if: booking is CONFIRMED, within time window, and not yet checked in
}
```
Reference: `BookingRepository.java:107-126`

**Check-out** (Lines 231-245):
```java
@Transactional
public CheckOutResponse checkOut(String userId, String bookingId) {
    Instant now = Instant.now();
    int updated = bookingRepository.checkOut(bookingId, userId, now, BookingStatus.CONFIRMED, BookingStatus.COMPLETED);
}
```
Reference: `BookingRepository.java:128-147`

---

## **SLIDE 5: Payment Synchronization & Idempotency**

**Visual Content:**
- Dual-path diagram:
  - Synchronous path: User → Stripe Checkout → Success Page
  - Asynchronous path: Stripe → Webhook

**Code References:**

### **Synchronous Path: User Redirect**

**File:** `be/src/main/java/com/app/web/PaymentMvcController.java`

**Step 1: Initiate Checkout** (Lines 23-46):
```java
@GetMapping("/checkout")
public String checkout(
    @RequestParam String bookingId,
    @AuthenticationPrincipal DbUserDetails user,
    RedirectAttributes redirectAttributes
) {
    if (!stripePaymentService.isReady()) {
        redirectAttributes.addFlashAttribute("errorMessage", "Stripe not configured...");
        return "redirect:/bookings";
    }
    try {
        String url = stripePaymentService.createCheckoutSession(bookingId, user.getUserId());
        return "redirect:" + url;  // Redirect to Stripe Checkout
    } catch (StripeException ex) {
        redirectAttributes.addFlashAttribute("errorMessage", "Stripe error: " + ex.getMessage());
        return "redirect:/bookings";
    }
}
```

**Step 2: Create Stripe Session**
**File:** `be/src/main/java/com/app/service/StripePaymentService.java:54-104`

```java
@Transactional
public String createCheckoutSession(String bookingId, String userId) throws StripeException {
    // Verify booking is PENDING and payment is PENDING
    BookingEntity booking = bookingRepository.findById(bookingId)
        .filter(b -> b.getDeletedAt() == null && userId.equals(b.getUserId()))
        .orElseThrow(...);
    
    if (booking.getStatus() != BookingStatus.PENDING) {
        throw new IllegalStateException("Booking is not awaiting payment.");
    }
    
    // Build Stripe Session with success/cancel URLs
    String base = appProperties.getPublicBaseUrl().replaceAll("/$", "");
    String successUrl = base + "/payment/success?session_id={CHECKOUT_SESSION_ID}";
    String cancelUrl = base + "/payment/cancel?bookingId=" + bookingId;
    
    SessionCreateParams params = SessionCreateParams.builder()
        .setMode(SessionCreateParams.Mode.PAYMENT)
        .setClientReferenceId(bookingId)  // <-- Maps session back to booking
        .setSuccessUrl(successUrl)
        .setCancelUrl(cancelUrl)
        .addLineItem(
            SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(
                    SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(booking.getCurrency().toLowerCase())  // "usd"
                        .setUnitAmount((long) booking.getAmountTotal())     // cents
                        .setProductData(
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName("Parking booking")
                                .build())
                        .build())
                .build())
        .putMetadata("bookingId", bookingId)      // <-- Metadata for webhook
        .putMetadata("paymentId", payment.getId())
        .putMetadata("userId", userId)
        .build();
    
    Session session = Session.create(params);
    
    // Store Stripe session ID in payment record
    payment.setProviderTxnRef(session.getId());
    payment.setUpdatedAt(Instant.now());
    paymentRepository.save(payment);
    
    return session.getUrl();  // Return Stripe checkout URL
}
```

**Step 3: Success Callback**
**File:** `be/src/main/java/com/app/web/PaymentMvcController.java:48-63`

```java
@GetMapping("/success")
public String success(
    @RequestParam("session_id") String sessionId,
    @AuthenticationPrincipal DbUserDetails user,
    RedirectAttributes redirectAttributes
) {
    try {
        stripePaymentService.fulfillFromCheckoutSessionId(sessionId, user.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Payment successful. Your booking is confirmed.");
    } catch (StripeException ex) {
        redirectAttributes.addFlashAttribute("errorMessage", "Could not verify payment: " + ex.getMessage());
    }
    return "redirect:/bookings";
}
```

**Verification Logic** (Lines 106-125):
```java
@Transactional
public void fulfillFromCheckoutSessionId(String sessionId, String userId) throws StripeException {
    // Query Stripe API to verify payment
    Session session = Session.retrieve(sessionId);
    if (!"paid".equals(session.getPaymentStatus())) {
        log.warn("Checkout session {} payment_status={}", sessionId, session.getPaymentStatus());
        return;  // Not yet paid
    }
    
    String bookingId = resolveBookingId(session);  // Extract from clientReferenceId or metadata
    BookingEntity booking = bookingRepository.findById(bookingId)
        .filter(b -> b.getDeletedAt() == null)
        .orElseThrow(...);
    
    if (!booking.getUserId().equals(userId)) {
        throw new IllegalArgumentException("Booking does not belong to current user.");
    }
    
    PaymentEntity payment = paymentRepository.findFirstByBookingIdAndDeletedAtIsNullOrderByCreatedAtDesc(bookingId);
    
    // Mark as paid (IDEMPOTENT)
    markBookingPaidFromStripe(session, booking, payment);
}
```

### **Asynchronous Path: Webhook Handler**

**File:** `be/src/main/java/com/app/web/PaymentWebhookController.java:27-45`

```java
@PostMapping("/webhook")
public ResponseEntity<String> webhook(
    @RequestBody String payload,
    @RequestHeader(value = "Stripe-Signature", required = false) String stripeSignature
) {
    if (stripeSignature == null || stripeSignature.isBlank()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Stripe-Signature");
    }
    try {
        stripePaymentService.handleWebhookPayload(payload, stripeSignature);
    } catch (SignatureVerificationException e) {
        log.warn("Invalid Stripe webhook signature: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
    }
    return ResponseEntity.ok("ok");  // Always return 200 to prevent Stripe retries
}
```

**Webhook Processing** (Lines 152-186):
```java
@Transactional
public void handleWebhookPayload(String payload, String sigHeader) throws SignatureVerificationException {
    if (!stripeProperties.isWebhookConfigured()) {
        throw new IllegalStateException("Stripe webhook secret not configured.");
    }
    
    // CRITICAL: Verify Stripe signature (HMAC-SHA256)
    Event event = Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());
    
    switch (event.getType()) {
        case "checkout.session.completed" -> {
            Session session = toSession(event);
            if (session != null && "paid".equals(session.getPaymentStatus())) {
                String bookingId = resolveBookingId(session);
                BookingEntity booking = bookingRepository.findById(bookingId)
                    .filter(b -> b.getDeletedAt() == null)
                    .orElse(null);
                if (booking == null) {
                    log.warn("Webhook: booking {} not found", bookingId);
                    return;
                }
                PaymentEntity payment = paymentRepository.findFirstByBookingIdAndDeletedAtIsNullOrderByCreatedAtDesc(bookingId);
                
                // Mark as paid (IDEMPOTENT)
                markBookingPaidFromStripe(session, booking, payment);
            }
        }
        case "checkout.session.expired" -> {
            // Handle expired session
            expireCheckoutSession(session.getId());
        }
    }
}
```

### **Idempotency: markBookingPaidFromStripe()**

**File:** `be/src/main/java/com/app/service/StripePaymentService.java:188-212`

```java
private void markBookingPaidFromStripe(Session session, BookingEntity booking, PaymentEntity payment) {
    
    // IDEMPOTENT: If already paid, return immediately
    if (payment.getStatus() == PaymentEntity.PaymentStatus.PAID) {
        return;  // <-- CRITICAL: Prevents double-confirmation
    }
    
    if (payment.getStatus() != PaymentEntity.PaymentStatus.PENDING) {
        log.warn("Cannot mark paid: payment {} status {}", payment.getId(), payment.getStatus());
        return;
    }
    
    Instant now = Instant.now();
    
    // Mark payment as PAID
    payment.setStatus(PaymentEntity.PaymentStatus.PAID);
    payment.setPaidAt(now);
    payment.setUpdatedAt(now);
    
    // Store Stripe transaction ID (Payment Intent)
    String paymentIntent = session.getPaymentIntent();
    if (paymentIntent != null) {
        payment.setProviderTxnRef(paymentIntent);
    }
    paymentRepository.save(payment);
    
    // Transition booking from PENDING to CONFIRMED
    if (booking.getStatus() == BookingStatus.PENDING) {
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(now);
        bookingRepository.save(booking);
    }
    
    // Broadcast real-time update
    slotAvailabilityNotifier.notifyAreaChanged(booking.getParkingAreaId());
}
```

### **Why This Works:**
1. **Synchronous path arrives first:** User redirected, payment marked PAID, booking confirmed
2. **Webhook arrives second:** Checks `if (payment.getStatus() == PAID) return;` → idempotent, no double-update
3. **Webhook arrives first:** Payment marked PAID, booking confirmed; synchronous path does the same (idempotent)
4. **Both fail:** Session expires, background task marks payment FAILED and cancels booking

---

## **SLIDE 6: Real-Time Architecture & Automation**

**Visual Content:**
- WebSocket flow diagram: Booking Event → STOMP Broker → `/topic/slots` → Browser re-fetches availability
- Background scheduler timeline

**Code References:**

### **WebSocket Configuration (STOMP + SockJS)**

**File:** `be/src/main/java/com/app/config/WebSocketConfig.java:1-49`

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    // Step 1: Configure message broker
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");                     // Enable /topic destinations
        registry.setApplicationDestinationPrefixes("/app");        // Receive from /app
    }
    
    // Step 2: Register STOMP endpoint
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")       // CORS
            .withSockJS();                        // Fallback to HTTP long-polling if WebSocket unavailable
    }
    
    // Step 3: Configure message converters (JSON serialization)
    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);  // Spring Boot auto-configured ObjectMapper
        converter.setContentTypeResolver(resolver);
        messageConverters.add(converter);
        return false;
    }
}
```

### **Real-Time Slot Availability Notifier**

**File:** `be/src/main/java/com/app/service/SlotAvailabilityNotifier.java:1-25`

```java
@Component
public class SlotAvailabilityNotifier {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public SlotAvailabilityNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    public void notifyAreaChanged(String parkingAreaId) {
        if (parkingAreaId == null || parkingAreaId.isBlank()) {
            return;
        }
        // Broadcast lightweight event to all connected clients
        messagingTemplate.convertAndSend(
            "/topic/slots",
            new SlotTopicMessage(parkingAreaId, "BOOKING_CHANGED")
        );
    }
}
```

### **When Notifications Occur:**

**File:** `be/src/main/java/com/app/service/BookingService.java`

1. **After creating booking** (Line 173):
   ```java
   slotAvailabilityNotifier.notifyAreaChanged(request.parkingAreaId());
   ```

2. **After cancelling booking** (Line 217):
   ```java
   slotAvailabilityNotifier.notifyAreaChanged(booking.getParkingAreaId());
   ```

3. **After payment confirmed** (in StripePaymentService.java Line 211):
   ```java
   slotAvailabilityNotifier.notifyAreaChanged(booking.getParkingAreaId());
   ```

### **Client-Side (Frontend Integration)**

Frontend file: `be/src/main/resources/static/js/booking-live.js`

```javascript
// Connect to WebSocket
const stompClient = new StompJs.Client({
    brokerURL: 'ws://' + window.location.host + '/ws',
    reconnectDelay: 5000,
});

stompClient.onConnect = function(frame) {
    // Subscribe to slot availability topic
    stompClient.subscribe('/topic/slots', function(message) {
        const data = JSON.parse(message.body);
        console.log('Booking changed in area:', data.parkingAreaId);
        
        // Re-fetch availability for that area
        fetchSlotAvailability(data.parkingAreaId);
    });
};

stompClient.activate();
```

### **Background Automation: Expire Stale Pending Bookings**

**File:** `be/src/main/java/com/app/service/BookingService.java:247-278`

```java
@Transactional
public MapResult expireStalePendingBookings() {
    Instant now = Instant.now();
    // Pending bookings expire after 3 hours without payment
    Instant cutoff = now.minusMillis(PENDING_EXPIRY_MS);  // LINE 30: 3 * 60 * 60 * 1000
    
    List<BookingEntity> stale = bookingRepository.findAll().stream()
        .filter(b -> b.getDeletedAt() == null)
        .filter(b -> b.getStatus() == BookingStatus.PENDING)
        .filter(b -> b.getCreatedAt() != null && (b.getCreatedAt().equals(cutoff) || b.getCreatedAt().isBefore(cutoff)))
        .toList();
    
    if (stale.isEmpty()) return new MapResult(0);
    
    List<String> ids = stale.stream().map(BookingEntity::getId).toList();
    
    // Mark all as CANCELLED
    for (BookingEntity b : stale) {
        b.setStatus(BookingStatus.CANCELLED);
        b.setCancelledAt(now);
        b.setUpdatedAt(now);
        bookingRepository.save(b);
    }
    
    // Mark payments as CANCELLED
    for (String bookingId : ids) {
        paymentRepository.updateStatusByBooking(
            bookingId,
            List.of(PaymentEntity.PaymentStatus.PENDING),
            PaymentEntity.PaymentStatus.CANCELLED,
            now
        );
    }
    
    // Notify clients to refresh
    for (BookingEntity b : stale) {
        slotAvailabilityNotifier.notifyAreaChanged(b.getParkingAreaId());
    }
    
    return new MapResult(ids.size());
}
```

*(Note: This method is called by a @Scheduled task — typically configured via Spring scheduler)*

---

## **SLIDE 7: Security & Role-Based Access Control**

**Visual Content:**
- RBAC matrix table (ADMIN vs USER permissions)
- Security filter chain flow diagram

**Code References:**

### **Spring Security Configuration**

**File:** `be/src/main/java/com/app/config/SecurityConfig.java:1-67`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    // ============ PASSWORD ENCODING ============
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // Strength 12 = 2^12 iterations
    }
    
    // ============ AUTHENTICATION PROVIDER ============
    @Bean
    public AuthenticationProvider authenticationProvider(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);  // Load user from DB
        provider.setPasswordEncoder(passwordEncoder);         // Validate password hash
        return provider;
    }
    
    // ============ SECURITY FILTER CHAIN ============
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider)
        throws Exception {
        
        // Add authentication provider
        http.authenticationProvider(authenticationProvider);
        
        // CSRF Protection (disable for webhook, WebSocket, demo endpoints)
        http.csrf(csrf -> csrf.ignoringRequestMatchers(
            "/payment/webhook",
            "/ws/**",
            "/api/demo/**"
        ));
        
        // Request Authorization
        http.authorizeHttpRequests(auth -> auth
            // Forward/Error dispatches allowed (JSP forward without re-auth)
            .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
            
            // Public endpoints
            .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/error").permitAll()
            .requestMatchers("/payment/webhook", "/ws/**", "/api/demo/**").permitAll()
            
            // Admin endpoints require ADMIN role
            .requestMatchers("/admin/**").hasRole("ADMIN")  // LINE 48
            
            // All other endpoints require authentication
            .anyRequest().authenticated()
        );
        
        // Form Login Configuration
        http.formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .usernameParameter("email")                // Custom username field
            .passwordParameter("password")
            .defaultSuccessUrl("/dashboard", true)     // Redirect after success
            .failureUrl("/login?error")                // Redirect after failure
            .permitAll()
        );
        
        // Logout Configuration
        http.logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)               // Destroy session
            .deleteCookies("JSESSIONID")                // Remove JSESSIONID cookie
            .permitAll()
        );
        
        // Clickjacking protection
        http.headers(headers -> headers.frameOptions(frame -> frame.deny()));
        
        return http.build();
    }
}
```

### **User Details Service (Load User from Database)**

**File:** `be/src/main/java/com/app/security/DbUserDetailsService.java:1-25`

```java
@Service
public class DbUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    public DbUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository
            .findByEmail(email.trim().toLowerCase())  // Case-insensitive lookup
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Wrap in Spring Security UserDetails
        return new DbUserDetails(user);
    }
}
```

**File:** `be/src/main/java/com/app/security/DbUserDetails.java` (not shown; implements UserDetails interface)

### **User Registration (Password Hashing)**

**File:** `be/src/main/java/com/app/service/UserService.java:31-49`

```java
@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public void register(RegisterForm form) {
        // Validate passwords match
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Passwords do not match");
        }
        
        // Check email not already registered
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Email already registered");
        }
        
        // Create user with BCrypt-hashed password
        Instant now = Instant.now();
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID().toString().replace("-", ""));
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));  // BCrypt hashing
        user.setRole(UserEntity.UserRole.USER);                            // Default role
        user.setActive(true);
        user.setCreatedAt(now);
        userRepository.save(user);
    }
}
```

### **Admin RBAC Example: AdminParkingAreaController**

**File:** `be/src/main/java/com/app/web/AdminParkingAreaController.java:1-96`

```java
@Controller
@RequestMapping("/admin/areas")  // All routes require /admin/** → hasRole("ADMIN")
public class AdminParkingAreaController {
    
    @GetMapping
    public String list(Model model) {
        // Only ADMIN can reach here (filtered by SecurityFilterChain)
        model.addAttribute("areas", parkingAreaService.listAllForAdmin());
        return "admin/areas";
    }
    
    @GetMapping("/create")
    public String createForm(Model model) {
        // Only ADMIN
        return "admin/area-form";
    }
    
    @PostMapping
    public String create(
        @RequestParam String name,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String addressLine1,
        RedirectAttributes ra
    ) {
        // Only ADMIN can POST to /admin/areas
        try {
            parkingAreaService.create(city, name, addressLine1);
            ra.addFlashAttribute("msgSuccess", "Parking area created.");
        } catch (ApiException e) {
            ra.addFlashAttribute("msgError", e.getMessage());
        }
        return "redirect:/admin/areas";
    }
    
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, RedirectAttributes ra) {
        // Only ADMIN can delete
        parkingAreaService.disable(id);
        ra.addFlashAttribute("msgSuccess", "Parking area deactivated.");
        return "redirect:/admin/areas";
    }
}
```

### **RBAC Authorization Matrix**

| Endpoint | Public | USER | ADMIN | Method |
|----------|--------|------|-------|--------|
| `/` | ✓ | ✓ | ✓ | GET |
| `/login` | ✓ | ✓ | ✓ | GET/POST |
| `/register` | ✓ | ✓ | ✓ | GET/POST |
| `/bookings` | ✗ | ✓ | ✓ | GET/POST |
| `/bookings/new` | ✗ | ✓ | ✓ | GET/POST |
| `/payment/checkout` | ✗ | ✓ | ✓ | GET |
| `/admin/**` | ✗ | ✗ | ✓ | GET/POST |
| `/admin/areas` | ✗ | ✗ | ✓ | GET/POST/DELETE |
| `/admin/slots` | ✗ | ✗ | ✓ | GET/POST/DELETE |
| `/admin/users` | ✗ | ✗ | ✓ | GET (read-only) |
| `/api/demo/**` | ✓ | ✓ | ✓ | GET (educational) |
| `/ws` | ✓ | ✓ | ✓ | WebSocket |
| `/payment/webhook` | ✓ | ✓ | ✓ | POST (Stripe only) |

---

## **SLIDE 8: Team Collaboration & Engineering Practices**

**Visual Content:**
- Documentation folder structure
- Configuration example

**Code References:**

### **Documentation Structure**

**Directory:** `ParkingBookingSystem/docs/`

Key files:
- `chạy.md` — Step-by-step local setup guide
- `sql.md` — SQL coursework explanation (JOINs, EXISTS, GROUP BY)
- `erd-smart-parking.md` — Entity-Relationship Diagram (DBML format)
- `he-thong-fe-be-db-flow.md` — System architecture overview
- `deployment-guide.md` — Production deployment runbook
- `incident-runbook.md` — Incident response procedures
- `principles-of-database-management-project-report.md` — PDM course report

### **Configuration Management: .example Files**

**File:** `be/src/main/resources/application-local.yml.example`
- Template for local development
- Developers copy to `application-local.yml` (ignored in `.gitignore`)
- Prevents accidental credential leaks to GitHub

**File:** `be/src/main/resources/application.yml:1-52`

Environment variables:
```yaml
DATABASE_URL: ${DATABASE_URL:jdbc:mysql://localhost:3306/smart_parking}
DATABASE_USER: ${DATABASE_USER:root}
DATABASE_PASSWORD: ${DATABASE_PASSWORD:root}
STRIPE_SECRET_KEY: ${STRIPE_SECRET_KEY:}
STRIPE_WEBHOOK_SECRET: ${STRIPE_WEBHOOK_SECRET:}
APP_PUBLIC_BASE_URL: ${APP_PUBLIC_BASE_URL:http://localhost:8080}
SERVER_PORT: ${SERVER_PORT:8080}
JPA_DDL_AUTO: ${JPA_DDL_AUTO:validate}
SQL_INIT_MODE: ${SQL_INIT_MODE:always}
```

### **Automated Database Initialization**

**File:** `be/src/main/resources/schema.sql` (Lines 1-77)
- Runs once on startup
- Creates all tables with constraints

**File:** `be/src/main/resources/data.sql`
- Inserts seed data (demo areas, slots, users)
- Enables instant project startup

### **Version Control & Branching**

Root: `ParkingBookingSystem/.git/`
- Maven-built JAR in `be/target/` (excluded from repo)
- Source code in `be/src/`

### **Build & Deployment**

**File:** `be/pom.xml:88-95`
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

Build command:
```bash
cd be
mvn clean package  # Creates be/target/parking-booking-0.0.1-SNAPSHOT.jar
```

---

## **SLIDE 9: Conclusion & Q&A**

**Visual Content:**
- Summary slide with key takeaways

**Code Summary:**

This backend demonstrates:
- ✅ **Concurrency Control:** SERIALIZABLE transactions eliminate race conditions
- ✅ **Payment Resilience:** Dual-path synchronization (sync + async webhooks) with idempotency
- ✅ **Real-Time Features:** STOMP WebSocket for instant slot availability updates
- ✅ **Security:** BCrypt passwords, role-based access control, CSRF/clickjacking protections
- ✅ **Database Integrity:** Constraints, indexes, complex SQL (JOIN, EXISTS, GROUP BY)
- ✅ **Team Practices:** Documentation, environment config templates, automated DB init

---

## **Quick File Index**

| Topic | File Path |
|-------|-----------|
| Spring Boot Entry | `be/src/main/java/com/app/Application.java` |
| Tech Stack | `be/pom.xml` |
| Database Schema | `be/src/main/resources/schema.sql` |
| Security Config | `be/src/main/java/com/app/config/SecurityConfig.java` |
| Booking Service | `be/src/main/java/com/app/service/BookingService.java` |
| Stripe Payment | `be/src/main/java/com/app/service/StripePaymentService.java` |
| Payment Webhook | `be/src/main/java/com/app/web/PaymentWebhookController.java` |
| WebSocket Config | `be/src/main/java/com/app/config/WebSocketConfig.java` |
| Real-Time Notifier | `be/src/main/java/com/app/service/SlotAvailabilityNotifier.java` |
| Admin RBAC | `be/src/main/java/com/app/web/AdminParkingAreaController.java` |
| SQL Demos (PDM) | `be/src/main/java/com/app/web/QueryDemoRestController.java` |
| Booking Repository | `be/src/main/java/com/app/repository/BookingRepository.java` |
| App Config | `be/src/main/resources/application.yml` |

