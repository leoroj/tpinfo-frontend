/**
 * Copyright (C) 2013-2020 Lars Erik Röjerås
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package se.skoview.app

import pl.treksoft.kvision.Application
import pl.treksoft.kvision.pace.Pace
import pl.treksoft.kvision.panel.root
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.redux.createReduxStore
import pl.treksoft.kvision.require
import pl.treksoft.kvision.startApplication
import pl.treksoft.kvision.utils.perc
import se.skoview.data.*
import se.skoview.view.HippoTablePage
import se.skoview.view.setUrlFilter
import kotlin.browser.window

// todo: Verifiera att zip bygger en produktionsversion
// done: Addera licensinformationen
// done: Fixa BASEURL så att den inte är hårdkodad mot tpinfo-a
// done: Fixa så det går att kopiera text utan att itemet väljs bort
// done: Lite hjälptexter, troligen på egen sida (via knapp ev)
// todo: Testa i andra webbläsare och Win/Linux
// done: Höjden på datumraden
// done: Kolumnerna ändrar fortfarande bredd
// done: Fixa val och fritextsökning av plattformChains
// done: Fixa till URL-hanteringen
// done: Fixa så att cursorn anpassar sig
// done: Stödtext efter item 100
// done: Snygga till ramarna och marginalerna
// done: Byta plats på logiska adresser och producenter
// done: Fix bug reported by Annika about error in filtering. Due to getParams() should only return dates.
// done: Bug in url handling. http//hippokrates.se/hippo - it seems hippo is removed
// done: Gå igenom all kod, städa och refaktorera det viktigaste

// done: Check why the icon is not displayed when run from hippokrates.se
// done: Länk till statistiken
// done: Fixa till färgerna
// done: Aktivera Google Analytics

// todo: Steg 1 Driftsätt K-hippo (rhippo)
// todo: Steg 2 Lös detta med att visa SE
// todo: Steg 3 Tag fram mock för hur integrationer ska presenteras där det kan finnas flera LA
// todo: Steg 4 Lös trädklättringen, kanske mha HSA-trädet
// todo: Lägg till möjlighet att enkelt visa ett meddelande till användaren vid uppstart
// todo: Visa antal användare sensate 24 timmarna
// done: Publicera rhippo på hippokrates.se
// todo: Markera om ingen träff i sökning på nåt sätt
// todo: Mer detaljerad styrning av muspekaren
// Och efter produktionssättningen, i 1.1
// done: Förbättre svarstiderna för fritextsökningen
// done: Färger på rubrikerna
// done: Frys rubrikraden
// done: Lägg till knapp som tar bort gränsen över hur många items som visar. Dvs en "visa alla"-knapp som dyker upp isf den röda texten.

// Initialize the redux store
val store = createReduxStore(
    ::hippoReducer,
    getInitialState()
)

fun main() {
    startApplication(::App)
}

class App : Application() {
    init {
        require("css/hippo.css")
    }

    override fun start() {

        root("hippo") {

            vPanel {
                add(HippoTablePage)
            }.apply {
                width = 100.perc
            }
        }

        println("Executing on: ${window.location.hostname}")

        // A listener that sets the URL after each state change
        store.subscribe { state ->
            setUrlFilter(state)
        }

        Pace.init()
        loadBaseItems(store)
    }
}

