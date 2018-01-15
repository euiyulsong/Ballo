package main

import (
    "net/http"
    "gopkg.in/mgo.v2"
    "github.com/bradfitz/slice"
    "encoding/json"
    "gopkg.in/mgo.v2/bson"
    "fmt"
    "log"
    "os"
    "crypto/sha512"
    "encoding/base64"
)

const (
    headerContentType = "Content-Type"
    charsetUTF8 = "; charset=utf-8"
    contentTypeJSON = "application/json" + charsetUTF8
)

type DeviceID string

type LeaderboardOutput struct {
    DeviceID DeviceID `json:"-" bson:"_id"`
    Name string `json:"name"`
    Score int `json:"score"`
}

type LeaderboardEntry struct {
    DeviceID DeviceID `json:"deviceID" bson:"_id"`
    Name string `json:"name"`
    Score int `json:"score"`
}

type Leaderboard struct {
    C *mgo.Collection
}

func (l *Leaderboard) scoreHandler(w http.ResponseWriter, r *http.Request) {
    switch r.Method {
    case http.MethodGet:
        // Return leaderboard data

        // Fetch all results
        result := []*LeaderboardOutput{}
        err := l.C.Find(nil).All(&result)
        if err != nil {
            http.Error(w, "Failed to fetch from database: " + err.Error(), http.StatusInternalServerError)
            return
        }

        // Sort this slice
        slice.Sort(result[:], func(i, j int) bool {
            // Reverse sort
            return result[i].Score > result[j].Score
        })

        // Write as json
        jsonString, err := json.Marshal(result)
        if err != nil {
            http.Error(w, "Failed to encode json: "+err.Error(), http.StatusInternalServerError)
            return
        }

        fmt.Printf("Fetched leaderboard for %d users\n", len(result))

        // Send
        w.Header().Add(headerContentType, contentTypeJSON)
        w.Write(jsonString)
        return

    case http.MethodPost:
        // Update or Insert a user's score

        // Decode request
        decoder := json.NewDecoder(r.Body)
        entry := &LeaderboardEntry{}
        if err := decoder.Decode(entry); err != nil {
            http.Error(w, "Failed to decode json: " + err.Error(), http.StatusBadRequest)
            return
        }

        // Hash id so we're not building a database of IMEIs lel
        hasher := sha512.New()
        hasher.Write([]byte(entry.DeviceID))
        entry.DeviceID = DeviceID(base64.URLEncoding.EncodeToString(hasher.Sum(nil)))

        // Insert or update
        i, err := l.C.UpsertId(entry.DeviceID, bson.M{ "$set": entry })
        if err != nil {
            http.Error(w, "Failed to upsert entry: " + err.Error(), http.StatusInternalServerError)
            return
        }

        // Otherwise success
        fmt.Printf("Successfully upsert: %v\n", i)
    }
}

func main() {
    mongoAddr := os.Getenv("MONGO_ADDRESS")
    fmt.Println("Connecting to mongo at: " + mongoAddr)
    s, e := mgo.Dial(mongoAddr)
    if e != nil {
        log.Fatal("Couldn't connect to mongodb: " + e.Error())
        os.Exit(1)
    }

    leaderboard := &Leaderboard{
        C: s.DB("ballo").C("ballo-prod"),
    }

    http.HandleFunc("/", leaderboard.scoreHandler)
    fmt.Println("Listesning to * on 80...")
    log.Fatal(http.ListenAndServe(":80", nil))
}
