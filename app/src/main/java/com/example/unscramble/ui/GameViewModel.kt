package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel:ViewModel(){

    // Game UI State
    private val  _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private lateinit var currentWord : String

    private var usedWords:MutableSet<String> = mutableSetOf()

    var userGuess by mutableStateOf("")
        private set

    private fun pickRandomWordAndShuffleWord():String{
        currentWord = allWords.random()
        return if(usedWords.contains(currentWord)){
            pickRandomWordAndShuffleWord()
        }else {
            usedWords.add(currentWord)
            shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word:String):String{
        val tempWord = word.toCharArray()
        tempWord.shuffle()
        while (tempWord.equals(word)){
            tempWord.shuffle()
        }

        return String(tempWord)
    }

    fun updateUserGuess(guessedWord:String){
        userGuess = guessedWord
    }

    fun checkUserGuess(){
        if(userGuess.equals(currentWord,ignoreCase = true)){
            // User's guess is correct,
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        }else{
            // User's guess is wrong,
            _uiState.update {
                it.copy(
                    isGuessedWordWrong = true,
                    currentScrambledWord = pickRandomWordAndShuffleWord(),
                    currentWordCount = it.currentWordCount.inc(),
                    score = it.score.minus(10))
            }
        }

        // Reset user guess
        updateUserGuess("")
    }

    private fun updateGameState(updatedScore:Int){

        if (usedWords.size == MAX_NO_OF_WORDS){
            _uiState.update {
                it.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        }else {
            _uiState.update {
                it.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffleWord(),
                    score = updatedScore,
                    currentWordCount = it.currentWordCount.inc()
                )
            }
        }
    }

    fun skipWord(){
        updateGameState(_uiState.value.score)
        updateUserGuess("")
    }



    fun resetGame(){
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffleWord())
    }

    init {
        resetGame()
    }
}