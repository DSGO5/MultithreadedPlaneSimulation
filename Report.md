<div align="center">

# Software Architecture and Extensible Design

## Assignment 1 Report

**Name:** David Gordon  
**Student ID:** 21499346  
**Semester:** Semester 2, 2025

</div>

---

## Table of Contents

- [(A) The way I used multithreading to create a solution to the Problem Description]()
- [(B) The challenges of a modified version of the problem]()
- [(C) Architecture]()

---

### (A) The way I used multithreading to create a solution to the Problem Description:

</br>

#### Which classes are responsible for starting threads, and what are the threads used for?

- #### Airport.java:
  - **Generator Thread**: Calls the StandardFlightRequests.go() for each airport to continuously generate Airports to send flights to. Puts generated destination airport ID's into an output stream.
   - **Producer Thread**: Uses a BufferedReader to read destination airport ID's and puts them into the airports destinationQ LinkedBlockingQueue.
   - **Dispatcher Thread**: Takes a destination from destinationQ and a ready plane (not being serviced) from availPlanes LinkedBlockingQueue then calls App.reequestStartFlight to send the plane to the destination airport.
   - **Servicing Thread(s)**: While these threads aren't created here, thery are used here. The servicePlane() method submits a task to shared threadPool ExecutorService to run the StandardPlaneServicing.service() method for each landed flight. After thread is done servicing (sleeping). It updates the planes fields to current airport and makes it available to be used for a flight.
 - #### App.java: 
   - **threadPool (ExecutorService)**: created with newCachedThreadPool() to have unlimited threads for plane servicing. Passed to each airport to service inbound planes.

</br>

#### How do the threads communicate?
- **Generator -> Producer**: PipedOutputStream
  - Generator Thread writes random destination airport ID's to a PipedOutputStream.
  - Producer Thread reads the ID's from the PipedInputStream connected to the PipedOutputStream using a BufferedReader.
- **Producer -> Dispatcher**: BlockingQueue
  - Producer Thread puts the recieved destination airport ID's into the destinationQ BlockingQueue.
  - Dispatcher Thread takes from the destinationQ BlockingQueue and sends an available plane to distnation airport.
- **Servicing Thread -> Dispatcher**: BlockingQueue
  - Servicing Thread offers planes that arrive at their destination airport into the availPlanes BlockingQueue after they're done servicing.
  - Dispatcher Thread takes from the availPlanes and sends them to destination airports.

</br>

#### How do the threads share resources (if they do at all) without incurring race conditions or deadlocks?

I strictly used inbuilt thread safe data structures such as PipedOutputStream/PipedInputStream (given) and BlockingQueues for communication and sharing resources between threads. These inbuilt data structures allow threads to wait(block) until items become available before accessing them, preventing any race conditions.

Deadlocks are also avoided due to not using any explicit locks or synchronized blocks. Each of the BlockingQueues are managed seperately by Offer/Put and take at each end of the communication between threads.

</br>

#### How do threads end?

the App.stopsim() method safely ends all of the threads and stops the overall simulation.
```java
private static void stopSim(JLabel statusText)
    {
        // safely end all airport threads
        for (Airport a : airports)
        {
            a.stop();   
        }
        //stop timer
        if (uiTimer != null)
        {
            uiTimer.stop();
        } 
        // end thread pool
        threadPool.shutdownNow();          
        statusText.setText("Stopped");
    }
```

 This method works by:

- Calls the stop() method for each airport:

  - the stop() method ends each of the threads used by the airport by calling their respective interrupt() method. It also offers the poison to the destinationQ so that the dispatcher exits its loop effectively.

  - ```java
    public void stop()
    {
        destinationQ.offer(poison);
        if(generator != null)
        {
            generator.interrupt();
        } 
        if(producer != null)
        {
            producer.interrupt();
        } 
        if(dispatcher != null)
        {
            dispatcher.interrupt();
        } 
    } 

- Stops the UI timer to freeze the animation.

- Calls the threadPools shutdownNow() method. This will reject new tasks and interrupt running ones.

Ending the threads this way is safe as it doesn't directly kills the thread but makes any blocking call it's in throw an InterruptedException which is caught in my code.

---

### (B) The challenges of a modified version of the problem

</br>

#### Assumptions:

- Real Time multiplayer through Websockets.
- All Game Rules and Processing is done server side. Client only sends inputs and render snapshots each tick.
- Paymennts are done by Third Party
- Transport Security Exists in all forms of communication using TLS
- External Payment Service Provider handles all card details and processing. Alerts server for payment completion.
- Each region has its own gateway and services and data tier.
- All Databases will be operated with security measures.

<br/>

#### Non-Functional Requirements (NFR):

- **Gameplay needs to have Low Latency**: Keep round-trip delay and jitter low so multiplayer is fair and responsive.
- **Measure and be able to display statistics such as Latency, Packetloss, Errors, etc**: Continuously measure latency, traffic, errors so you can detect issues quickly and provide reliability.
- **Payments need to be securely processed**: To ensure no payment details are at risk of being stolen.
- **Protect against double charges**: so players aren’t charged twice during retries or network glitches to presere trust.
- **AntiCheat**: To ensure fairness in gameplay and so competition is fair and players don’t stop playing the game due to cheating.
- **Personal Data needs to be protected**: so personal data is stored and processed lawfully and securely, avoiding legal risk and user harm by a data leak.
- **Purchases need ot be auditable**: so you can resolve any purchasing issues and investigate any changes in purchasing habits.
- **Needs to have Accessability settings**: so players with disabilities can use the UI and play the game.
- **Needs to have Localisation**: so players can understand content (language, currency).
- **Should have region based servers**: so that players are matched to regional servers to reduce latency.
- **Should have backups of game and player data**: to restore service and player progress after incidents.
- **Should be Maintainable and modifiable**: so developers can add features, fix bugs, and iterate quickly without causing regressions.
- **Should have low end hardware requirements**: so that the game runs on a wide range of devices to broaden the audience.

---

### (B) Architecture

<br/>

#### Diagram:

![Mulitplayer_Airplane_Architecture_Diagram](/SAED_Assignment1_Diagram.png)

#### Explanation:

The GUI is the UI for the players which is facilitated through the Game Client. The client handles accessability and localisation itself based on user preference. It also renders the visuals, gets user input and opens 2 network paths via the communication layer:

The Websocket is a fully duplex connection that the session manager uses to push snapshots/ticks to the client and the client usus it to send inputs back.

The API Gateway uses REST calls for gateway features such as authentication, throttling, browsing store or udpating account details and so on.

The Load balancer distributes new sessions to the nearest and most available regional game server and attaches the client to the servers authentication service.

The authentication service maintains communication with the Websocket and HTTP calls from the API Gateway and uses tokens to validate every request and update so only valid and authenticated data/calls reach the game services.

The game services inside each regional sever do a wide range of things: 

- Session Manager: runs a real-time game loop and validates the inputs form players and updates the worlds state.
- Matchmaker: groups players in a regional server and stores logs in the MatchmakerDB.
- Game Logic: Shared engine code used by the session manager for rules, plane movement, route planning and scoring.
- Anti-Cheat: recieves events from session manager and validates it against the rules and detects cheating.
- Store-Service: Manages purchasing option, and talks to the external payment gateway for any pruchases. Sends data to Databases and doesn't store any card data.
- Monitoring/Analytics: emits latency, traffic, packet loss and any errors and gameplay metrics such as planes in flight, completed trips and other player dashboard information back to the client.
- Backup Agent: manages backups by sending snapshots of all game data and state to stateful databases such as BackupandAuditDB.

The External Services are:

- Payment Gateway: handles user end of card capture and settlements externally. Communicates with store service to ensure retries and timeouts don't result in double-charges.
- Redis Cache: Used by many game services for  real time gameplay:
  - Can store players session data
  - Used by matchmaking to save a queue of players waiting to join a match
  - Stores server's live data such as leaderboards and current flight data
  - Used by Anticheat to cache actions and patterns for real time cheat detection. Also used to track API calls and login attempts to prevent abuse.
  - Used by store service to save shopping cart state.
  - Used by analytics and monitoring to cache real time data.
  - Used to optimise preformance by caching player data.

The Regional Servers and the game services communicate with the databases to log and preform their functions.

The PlayerGameDB is used to store all player information that is relevant to the game such as score/money, levels, planes, inventory and settings, etc. Mainly used by the Session Manager and Store Service. This is also the source of truth for player's game related data.

The PlayerInformationDB is used to store player identity information such as AccountID, username, email, password, region, etc. This information is kept seperate to the players game information as it doesn't change as often as the player's game data. It is mainly used by the authentication service and any service wanting to use or updateplayer information.

MatchmakerDB is used by the Matchmaker and Session Manager only and stores session tickets and player ratings which are used to let players play together.

The OrdersDB is the purchases ledger which stores an auditable trail of every purchase done by players. It doesn't store any card details and is only used for disputes and validating purchases.

Backup and Audit DB is used to store regular snapshots of the Game and versions of all other DB's. It is meant to be kept long term and is used as an Audit log for any actions. It is mainly used by the backup agent and admins.

The databases are used as the main source of truth over the Redis Cache and any incosistent data is checked using the database if necessary.

#### How the Architectural Approach Support my Non-Functional Requirements:

- Low latency gameplay: Redis and WebSockets are used for real time updates to ensure smooth and fast gameplay. MatchmakerDB and the session manager stay small so matches spin up fast.

- Measure & display stats: Statistics are messured by the Regional Server for each player and sent back to the client using the WebSocket for real time updates.

- Secure payments & no double charges: External Payment Provider is called by the Store Services. Payment Provider handles card information and processing and reduces our servers exposure to sensitive information. External Payment Provider also prevents double charges by communicating with Store Service to validate purchases using keys.

- Fair play / anti-cheat: The client doesn't handle any game logic and therefore client side cheating is minimised. Server side detection is also in place with the anti-cheat.

- Personal data protection: Sensitive Player information is in PlayerInformationDB only. gameplay data don’t expose personal details, limiting bexposure and helping compliance.

- Auditable purchases: OrdersDB keeps a clean history of each attempt (status changes + External Payment Provider reference). That makes disputes/refunds traceable and supports finance audits.

- Accessability: Client UI is customisable as per the users needs.

- Localisation: Client loads and saves regional language packs seperate from the server. This allows users to select their language and other regional settings manually.

- Backups & graceful ops: The Backup Store holds restorable snapshots. Stateful DB's are backed up regularly and is abled to be used to restore to last back up if needed.

- Region-based servers: Players connect to the closest region to reduce lag and jitter. The load balancer and matchmaker use simple latency-aware configurations to route players.

- Low Hardware Requirements: All of the heavy simulation and validation runs on the server side, minimising client side requirements.

#### Disadvantages:

1. Having Multiple regions, several services and caches means more things to deply to and patch/maintain.
2. Having all of the processing and other services be serverside means more operational costs.
3. Need to manage a large number of WebSockets to maintain playability.
4. Real time statistics monitoring may be difficult.
5. Deploying Updates across regions may require a careful and time consuming process which may disrupt playability or even data loss.