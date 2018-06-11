# word-herd
Sample App with Spring, Kotlin, React, Websockets, Oauth2: herd your words.

# Learning Techs
I know Spring pretty well.  I want to re-learn spring with Kotlin.  I also want to learn more about React, Websockets, and Oauth2. This app will incorporate all these technologies.

I also want to experiment with not having a database.  Databases are great!  But having one seems like a default assumption that's worth questioning.  Instead of using a database, I want to use Oauth2 providers that also offer storage, like GitHub, Amazon/S3, or Google/GCS.  I think this will be good because:
 - fewer trust boundaries for the admin (me) to think about
 - fewer scalability/reliability/HA issues for the admin (me) to think about
 - notifications when data change
 - users will own their data at all times -- the app is just a guest

# Herd your Words
This app will also allow users to herd their words!

This means users can create and view a collection of words with definitions, stored in their own storage.  A simple sample app.

An outline of the app:
 - Anyone can visit a "welcome" page and try to log in with e.g. GitHub.
 - OAuth request will include scopes to read and write from e.g. a repo.
 - Logged-in users will return to an "everything-else" page.
 - Users must provide a repo name.
 - The everything-else page will display any existing Word records it finds in the repo, in alphabetical order.
 - Users can submit a new word to add to the herd.
 - New words will be provisionally added to the herd in a "pending" state.
 - On the server side, the word will be looked up at e.g. wiktionary.
 - Word lookup will be async in the background.
 - When a word lookup succeeds, it will be written as a Word record, with its definition, to the repo.
 - When a word lookup fails, it will not be added.
 - Either way, the client will be notified and the everything-else page will be updated.
 - Users can log out from the everything-else page.
 - This will invalidate their OAuth token and return them to the "welcome" page.
