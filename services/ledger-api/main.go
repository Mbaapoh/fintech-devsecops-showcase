package main

import (
	"fmt"
	"net/http"
)

func main() {
	fmt.Println("Ledger API Service Starting...")
	http.HandleFunc("/balance", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintf(w, "Secure Balance Check")
	})
	http.ListenAndServe(":8081", nil)
}
