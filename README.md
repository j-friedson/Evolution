# Evolution

This is my version of the game Evolution, created for the class [Software Development](http://www.ccs.neu.edu/home/matthias/4500-s16/), as taught by [Matthias Felleisen](http://www.ccs.neu.edu/home/matthias/) in the spring of 2016. I took the class as a sophomore at Northeastern University and since then have been trying to maintain this as a personal project in my free time, although my lack of free time has made that slightly more challenging than I anticipated. 

My current goals for the project are as follows:
  1. Finish writing appropriate documentation so the code base can be easily understood by someone brand new to it
  2. Fix known bugs, and write a more thorough test-suite to find unknown bugs
  3. Develop a (better) automated player strategy using machine learning algorithms (see strategy branch)
  
It's important to note that although Evolution is in fact a [real board game](https://boardgamegeek.com/boardgame/155703/evolution), our version used modified rules which can be found [here](http://www.ccs.neu.edu/home/matthias/4500-s16/evolution.html).

Files and Directories
-----------------------
| Name          | Purpose                                    |
|---------------|--------------------------------------------|
| lib           | contains third party libraries |
| src/evo       | contains source code |
| src/test      | contains JSON test files |
| compile       | script to compile the code |
| xclient       | script to run remote client program |
| xmain         | script to run local program |
| xserver       | script to run remote server program |
| xtest         | script to run JSON integration tests |

Compiling and Running
------------------
To compile the code:
> ./compile

To run the main program:
> ./xmain [3-8]

Where [3-8] is an integer representing the number of players, between 3 and 8 (inclusive)

Testing
-----------------
To run the integration tests:
> ./xtest [type]

Where [type] is one of:
  - attack
  - choose
  - feed
  - step
  - step4
  
This will run the appropriate integration test script and its associated files. All tests should pass except for matthias-6 and matthias-8 in step4. These tests fail because of a minor bug involving the order in which cards are added to the hand, which doesn't affect gameplay in practice.

The original unit tests have not been included in this repo, but once I have time to create a new (more thorough) test suite I'll upload it.

Running the Distributed (Remote) Version
----------------------------------------
To run the server program:
> ./xserver [port]

Where [port] is an optional argument specifying the port of the machine to be used (default is 45678)

To run the client program:
> ./xclient [host] [port]

Where [host] is an optional argument specifying the host address to connect to (default is localhost), and [port] is an optional argument
specifying the port on the server machine to connect to (default is 45678)

Once started, the server program will listen for incoming connections on the specified port. When three client programs have signed up, it will wait three seconds for other players to sign up (the default wait time can be modified by changing the SIGNUP\_DELAY variable in src/evo/Constants.java). After three seconds have elapsed, or if five more players sign up while the program is waiting, the game will start. If at any time a client player takes more than 10 seconds to respond to a server request (can be modified via the MAX\_RESPONSE\_TIME variable), or a client player gives an illegal response (see the [rules](http://www.ccs.neu.edu/home/matthias/4500-s16/evolution.html) for what consitutes an illegal response), that player will be kicked, and the game will continue without the player.
