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
package se.skoview.stat

import pl.treksoft.kvision.chart.*
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.form.check.checkBoxInput
import pl.treksoft.kvision.form.select.simpleSelectInput
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.modal.Modal
import pl.treksoft.kvision.modal.ModalSize
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.flexPanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.state.bind
import pl.treksoft.kvision.table.TableType
import pl.treksoft.kvision.table.cell
import pl.treksoft.kvision.table.row
import pl.treksoft.kvision.table.table
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.utils.vh
import pl.treksoft.kvision.utils.vw
import se.skoview.app.formControlXs
import se.skoview.app.store
import se.skoview.common.*

object StatPage : SimplePanel() {

    init {
        println("In CharTab():init()")

        this.marginTop = 10.px

        // Page header
        div {
            h2("Antal meddelanden genom SLL:s regionala tjänsteplattform")
            div("Detaljerad statistik med diagram och möjlighet att ladda ner informationen för egna analyser.")
        }.apply {
            //width = 100.perc
            id = "pageHeaderDiv"
            background = Background(Color.hex(0x113d3d))
            align = Align.CENTER
            color = Color.name(Col.WHITE)
            marginTop = 5.px
        }

        flexPanel(
            FlexDirection.ROW, FlexWrap.WRAP, JustifyContent.SPACEBETWEEN, AlignItems.CENTER,
            spacing = 5
        ) {
            id = "controlPanelFlex"
            clear = Clear.BOTH
            margin = 0.px
            background = Background(Color.hex(0xf6efe9))
            div {
                align = Align.LEFT
            }.bind(store) { state ->
                println("After bind in header")
                table(
                    listOf(),
                    setOf(TableType.BORDERED, TableType.SMALL)
                ) {
                    // Star tdate
                    row {
                        cell { +"Startdatum:" }
                        cell {
                            simpleSelectInput(
                                options = state.statisticsDates.sortedByDescending { it }.map { Pair(it, it) },
                                value = state.dateEffective
                            ) {
                                addCssStyle(formControlXs)
                                background = Background(Color.name(Col.WHITE))
                            }.onEvent {
                                change = {
                                    store.dispatch(HippoAction.DateSelected(DateType.EFFECTIVE, self.value ?: ""))
                                    loadStatistics(store.getState())
                                }
                            }
                        }
                        cell { +"Plattform:" }
                        cell {
                            val selectedPlattformId =
                                if (state.selectedPlattformChains.size > 0)
                                    PlattformChain.map[state.selectedPlattformChains[0]]!!.last.toString()
                                else ""

                            val options =
                                if (state.statAdvancedMode)
                                    state.statisticsPlattforms.map { Pair(it.key.toString(), it.value.name) }
                                else
                                    listOf(Pair("3", "SLL-PROD"))

                            println("Plattform select:")
                            console.log(options)
                            simpleSelectInput(
                                //options = state.statisticsPlattforms.map { Pair(it.key.toString(), it.value.name) },
                                options = options,
                                value = selectedPlattformId

                            ) {
                                addCssStyle(formControlXs)
                                background = Background(Color.name(Col.WHITE))
                            }.onEvent {
                                change = {
                                    val selectedTp = (self.value ?: "").toInt()
                                    tpSelected(selectedTp)
                                    /*
                                    val pChainId =
                                        PlattformChain.calculateId(first = selectedTp, middle = null, last = selectedTp)
                                    store.dispatch(HippoAction.ItemIdDeselectedAll(ItemType.PLATTFORM_CHAIN))
                                    store.dispatch(HippoAction.ItemDeselectedAllForAllTypes)
                                    //store.dispatch { dispatch, getState ->
                                    store.dispatch(
                                        HippoAction.ItemIdSelected(
                                            ItemType.PLATTFORM_CHAIN,
                                            pChainId
                                        )
                                    )
                                    loadStatistics(store.getState())
                                     */
                                }
                            }
                        }

                        // Show time graph
                        cell {
                            checkBoxInput(
                                value = state.showTimeGraph
                            ).onClick {
                                if (value) loadHistory(state)
                                store.dispatch(HippoAction.ShowTimeGraph(value))
                            }
                            +" Visa utveckling över tid"
                        }

                        // Use Advanced mode
                        cell {
                            checkBoxInput(
                                value = state.statAdvancedMode
                            ).onClick {
                                store.dispatch(HippoAction.StatAdvancedMode(value))
                                if (!value)
                                // todo: This hard coded plattform id must change to something more dynamic
                                    tpSelected(3)
                            }
                            +" Avancerat läge"
                        }
                    }

                    // End date
                    row {
                        cell { +"Slutdatum:" }
                        cell {
                            simpleSelectInput(
                                options = state.statisticsDates.sortedByDescending { it }.map { Pair(it, it) },
                                value = state.dateEnd
                            ) {
                                addCssStyle(formControlXs)
                                background = Background(Color.name(Col.WHITE))
                            }.onEvent {
                                change = {
                                    //store.dispatch { dispatch, getState ->
                                    store.dispatch(HippoAction.DateSelected(DateType.END, self.value ?: ""))
                                    loadStatistics(store.getState())
                                    //}
                                }
                            }
                        }

                        cell { +"Visa:" }
                        cell {
                            val selectedPreSelect = state.statPreSelect
                            val options =
                                if (state.statAdvancedMode)
                                    StatPreSelect.selfStore
                                        .filter { it.value.showInAdvancedView == true }
                                        .map { Pair(it.key, it.value.getLabel(state.statAdvancedMode)) }
                                    else
                                    StatPreSelect.selfStore
                                        .filter { it.value.showInSimpleView != null }
                                        .map { Pair(it.key, it.value.getLabel(state.statAdvancedMode)) }
                            simpleSelectInput(
                                options = options,
                                value = selectedPreSelect
                            ) {
                                addCssStyle(formControlXs)
                                background = Background(Color.name(Col.WHITE))
                            }.onEvent {
                                change = {
                                    val selectedPreSelect: String = self.value ?: "-"
                                    println("Selected pre-select: '$selectedPreSelect'")
                                    store.dispatch(HippoAction.PreSelectedSelected(selectedPreSelect))
                                    if (state.showTechnicalTerms) { // Restore technical labels
                                        store.dispatch(HippoAction.ShowTechnicalTerms(state.showTechnicalTerms))
                                    }
                                    store.dispatch(HippoAction.ItemDeselectedAllForAllTypes)
                                    val selectedItemsMap = StatPreSelect.selfStore[selectedPreSelect]!!.selectedItemsMap
                                    for ((itemType, itemIdList) in selectedItemsMap) {
                                        itemIdList.forEach {
                                            store.dispatch(HippoAction.ItemIdSelected(itemType, it))
                                        }
                                    }
                                    loadStatistics(store.getState())
                                }
                            }
                        }
                        cell {
                            checkBoxInput(
                                value = state.showTechnicalTerms
                            ).onClick {
                                //if (value) loadHistory(state)
                                println("In showTechnicalTerms, value = $value")
                                store.dispatch(HippoAction.ShowTechnicalTerms(value))
                                if (!value) { // Restore labels for current preselect
                                    store.dispatch(HippoAction.PreSelectedSelected(state.statPreSelect))
                                }
                            }
                            +" Tekniska termer"
                        }

                    }
                }
                val calls = state.statBlob.callsDomain.map { it.value }.sum()
                val tCalls = calls.toString().thousands()
                span { +"Totalt antal anrop för detta urval är: $tCalls" }.apply { align = Align.CENTER }
            }

            // About button
            vPanel {
                button("Exportera").onClick {
                    exportStatData(store.getState())
                }.apply {
                    addBsBgColor(BsBgColor.LIGHT)
                    addBsColor(BsColor.BLACK50)
                    marginBottom = 5.px
                }
                //background = Background(Col.LIGHTSKYBLUE)
                //align = Align.RIGHT
                val modal = Modal("Om Statistikfunktionen")
                modal.iframe(src = "about.html", iframeHeight = 400, iframeWidth = 700)
                modal.size = ModalSize.LARGE
                //modal.add(H(require("img/dog.jpg")))
                modal.addButton(Button("Stäng").onClick {
                    modal.hide()
                })
                button("Om Statistik ${getVersion("hippoVersion")}", style = ButtonStyle.INFO).onClick {
                    size = ButtonSize.SMALL
                    modal.show()
                }.apply {
                    addBsBgColor(BsBgColor.LIGHT)
                    addBsColor(BsColor.BLACK50)
                }

            }
        }

        div { }.bind(store) { state ->
            if (state.showTimeGraph && state.historyMap.isNotEmpty()) {
                val animateTime =
                    if (state.currentAction == HippoAction.DoneDownloadHistory::class) {
                        1298
                    } else {
                        -2
                    }

                println("Will display time graph")
                val xAxis = state.historyMap.keys.toList()
                val yAxis = state.historyMap.values.toList()
                chart(
                    Configuration(
                        ChartType.LINE,
                        listOf(
                            DataSets(
                                label = "Antal anrop per dag",
                                data = yAxis
                            )
                        ),
                        xAxis,
                        options = ChartOptions(
                            animation = AnimationOptions(duration = animateTime),
                            legend = LegendOptions(display = true),
                            responsive = true,
                            maintainAspectRatio = false
                        )
                    )
                ).apply {
                    height = 26.vh
                    width = 95.vw
                    //background = Background(Color.name(Col.AZURE))
                }
            }
        }

        div { }.bind(store) { state ->
            if (state.statAdvancedMode) add(AdvancedView)
            else add(SimpleView)
        }
    }

    private fun tpSelected(selectedTp: Int) {
        val pChainId =
            PlattformChain.calculateId(first = selectedTp, middle = null, last = selectedTp)

        // todo: Do not do anything if the function is called with the currently already selected TPId
        //if (store.getState().selectedPlattformChains.contains(selectedTp)) return

        store.dispatch(HippoAction.ItemIdDeselectedAll(ItemType.PLATTFORM_CHAIN))
        store.dispatch(HippoAction.ItemDeselectedAllForAllTypes)
        //store.dispatch { dispatch, getState ->
        store.dispatch(
            HippoAction.ItemIdSelected(
                ItemType.PLATTFORM_CHAIN,
                pChainId
            )
        )
        loadStatistics(store.getState())
    }
}


