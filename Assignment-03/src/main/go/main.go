package main

import (
	"fmt"
	"math/rand"
)

type BattleMessage struct {
	id       int
	random   int
	isWinner bool
}

func main() {
	var m int

	fmt.Print("Insert the number of rounds (2^m players): m = ")
	_, err := fmt.Scan(&m)
	if err != nil {
		return
	}
	N := 1 << m
	fmt.Printf("%d Rounds with %d players \n", m, N)
	fmt.Println("\nRules:\n\t- Leader is Left-Sided and always choose Even\n\t- Condition win: _+_ % 2\n\n")

	tournamentChannels := make([][]chan BattleMessage, m)

	// example: 2^3 == 8 players
	// P1 - P2 --> P2 								P6 <-- P5 - P6
	// 			 				| --> P2 - P8	<-- |
	// P3 - P4 --> P3 			P2(w)			P8 <-- P7 - P8
	// every '-' && '|' is a communication with two actor (a battle), total number of battles = 7
	// total channels for battles: [ch12, ch34, ch56, ch78, ch1234, ch5678, chFinal]
	for r := 0; r < m; r++ {
		// num_battles per round
		// Round 1 -> 8 / 2^1 = 4
		// Round 2 -> 8 / 2^2 = 2
		// Round 3 -> 8 / 2^3 = 1
		// Round x -> N / 2^x
		numBattles := N / (1 << (r + 1))
		tournamentChannels[r] = make([]chan BattleMessage, numBattles)
		for b := 0; b < numBattles; b++ {
			// Round 1 (4): [ch12, ch34, ch56, ch78]
			// Round 2 (2): [ch1234, ch5678]
			// Round 3 (1): [chFinal]
			tournamentChannels[r][b] = make(chan BattleMessage)
		}
	}

	// We need to assegnate for each player his correct channels
	// P1, P2 -> [ch12, ch1234, chFinal]
	// P3, P4 -> [ch34, ch1234, chFinal]
	// P7, P8 -> [ch78, ch5678, chFinal]
	// ... for each player
	for i := 0; i < N; i++ {
		playerChannels := make([]chan BattleMessage, m)
		for r := 0; r < m; r++ {
			// only channels for duos (ex. P6 and P5 have [ch56, ch5678, chFinal])
			// i = 5 (P6), numBattles = 2^(r+1)
			// Round 1 [ch12, ch34, ch56, ch78] -> 5 / 2 = 2 -> ch56
			// Round 2 [ch1234, ch5678] -> 5 / 4 = 1 -> ch5678
			// Round 3 [chFinal] -> 5 / 8 = 0 -> chFinal
			b := i / (1 << (r + 1))
			playerChannels[r] = tournamentChannels[r][b] // [ch56, ch5678, chFinal]
		}
		go Player(i, playerChannels)
	}

	for {
	}
}

// how to synchronize 2 players in a single channel?
func Player(id int, roundsChannels []chan BattleMessage) {
	isWinner := true
	for r := 0; isWinner && r < len(roundsChannels); r++ { // if notWinner silently go sleep
		// if Left Sided is Leader else Follower, ex:
		// P1 vs P2 -> P1: (0 / 2^0) = 0 (P1 Leader), P2: (1 / 2^0) = 1 (P2 Follower)
		// P2 vs P4 -> P2: (1 / 2^1) = 0 (P2 Leader), P4: (3 / 2^1) = 1 (P4 Follower)
		// P1 vs P7 -> P1: (0 / 2^2) = 0 (P1 Leader), P7: (6 / 2^2) = 1 (P7 Follower)
		// P3 vs P5 -> P3: (2 / 2^2) = 0 (P3 Leader), P5: (4 / 2^2) = 1 (P5 Follower)
		isLeader := (id/(1<<r))%2 == 0
		random := rand.Intn(20)
		if isLeader { // wait for input, after, compute result and send.

			msg := <-roundsChannels[r]
			isWinner = (msg.random+random)%2 == 0
			roundsChannels[r] <- BattleMessage{id: id, random: random, isWinner: isWinner}

			enemyId := msg.id
			idWinner := enemyId
			if isWinner {
				idWinner = id
			}
			fmt.Printf("[Round %d] P%d (%d) vs. P%d (%d) -> P%d\n", r+1, id+1, random, msg.id+1, msg.random, idWinner+1)
		} else { // send the random and wait for response
			roundsChannels[r] <- BattleMessage{id: id, random: random}
			result := <-roundsChannels[r]
			isWinner = !result.isWinner
		}
	}
}
