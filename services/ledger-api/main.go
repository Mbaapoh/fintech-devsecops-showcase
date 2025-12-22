package main

import (
	"fmt"
	"net/http"
)

func BalanceHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "Secure Balance Check")
}

func main() {
	fmt.Println("Ledger API Service Starting...")
	http.HandleFunc("/balance", BalanceHandler)
	http.ListenAndServe(":8081", nil)
}
