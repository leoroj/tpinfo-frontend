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

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.hPanel
import pl.treksoft.kvision.table.TableType
import pl.treksoft.kvision.tabulator.*
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.utils.vw
import se.skoview.app.showBackgroundColorsForDebug
import se.skoview.common.*

fun Container.showItemTables(state: HippoState) {
    hPanel(
        spacing = 1
    ) {
        id = "showItemTables"
        if (showBackgroundColorsForDebug) background = Background(Color.name(Col.YELLOW))

        marginTop = 5.px
        overflow = Overflow.HIDDEN
        width = 99.vw

        SInfo.createStatViewData(state)

        var numberOfColumns = 0
        if (state.showConsumers) numberOfColumns++
        if (state.showProducers) numberOfColumns++
        if (state.showContracts) numberOfColumns++
        if (state.showLogicalAddresses) numberOfColumns++

        if (numberOfColumns < 1) return@hPanel

        width =
            if (numberOfColumns == 1) 50.vw
            else 100.vw

        if (state.showConsumers)
            itemTablePanel(
                state,
                itemSInfoList = SInfo.consumerSInfoList,
                label = getHeading(state, ItemType.CONSUMER),
                numberOfColumns
            )

        if (state.showContracts)
            itemTablePanel(
                state,
                itemSInfoList = SInfo.contractSInfoList,
                label = getHeading(state, ItemType.CONTRACT),
                numberOfColumns
            )

        if (state.showProducers)
            itemTablePanel(
                state,
                itemSInfoList = SInfo.producerSInfoList,
                label = getHeading(state, ItemType.PRODUCER),
                numberOfColumns
            )

        if (state.showLogicalAddresses)
            itemTablePanel(
                state,
                itemSInfoList = SInfo.logicalAddressSInfoList,
                label = getHeading(state, ItemType.LOGICAL_ADDRESS),
                numberOfColumns
            )
    }
}

fun Container.itemTablePanel(
    state: HippoState,
    itemSInfoList: SInfoList,
    label: String,
    numberOfColumns: Int
) {
    if (numberOfColumns < 1) return

    val columnWidth =
        if (numberOfColumns == 1) 45.vw
        else (99.3 / numberOfColumns).vw // - 0.1

    val chartLabelTable =
        ChartLabelTable(
            state,
            itemSInfoList.recordList,
            "description",
            "color",
            "calls",
            label
        ).apply { width = columnWidth }

    div {
        id = "itemTablePanel:$label"
        if (showBackgroundColorsForDebug) background = Background(Color.name(Col.LIGHTSTEELBLUE))
        width = columnWidth
        add(chartLabelTable) // .apply { height = 70.perc }
    }
}

class ChartLabelTable(
    state: HippoState,
    itemSInfoList: List<SInfoRecord>,
    dataField: String = "description",
    colorField: String = "color",
    callsField: String = "calls",
    heading: String
) : SimplePanel() {

    init {
        id = "ChartLabelTable:SimpleTable"

        val size = itemSInfoList.size
        val linesTerm =
            if (size == 1) "rad"
            else "rader"

        val currentHeight =
            if (state.numberOfItemViewsSelected() == 1) StatPanelSize.tablePanelSingleCssHeight
            else StatPanelSize.tablePanelMultipleCssHeight

        println("currentHeight = $currentHeight")

        tabulator(
            data = itemSInfoList,
            types = setOf(TableType.BORDERED, TableType.STRIPED, TableType.HOVER, TableType.SMALL),
            options = TabulatorOptions(
                layout = Layout.FITCOLUMNS,
                pagination = PaginationMode.LOCAL,
                paginationSize = 1000,
                paginationButtonCount = 0,
                height = currentHeight,
                selectable = true,
                columns = listOf(
                    ColumnDefinition(
                        headerSort = false,
                        title = "$heading ($size $linesTerm)",
                        field = dataField,
                        headerFilter = Editor.INPUT,
                        headerFilterPlaceholder = "Sök ${heading.toLowerCase()}",
                        editable = { false },
                        widthGrow = 7,
                        formatterComponentFunction = { cell, _, item ->
                            // cell.apply { background = Background(Color.name(Col.ALICEBLUE)) }
                            val itemRecord = item.unsafeCast<SInfoRecord>()
                            var textToShow: String = itemRecord.description
                            HPanel {

                                div(rich = true) {
                                    background = Background(item.color)
                                    +"&nbsp;&nbsp;"
                                }

                                div(rich = true) {
                                    whiteSpace = WhiteSpace.PREWRAP
                                    wordBreak = WordBreak.BREAKALL
                                    if (state.isItemSelected(itemRecord.itemType, itemRecord.itemId)) {
                                        fontWeight = FontWeight.BOLD
                                        cell.apply {
                                            background = Background(Color.name(Col.LIGHTGRAY))
                                        }
                                        textToShow = "$textToShow (<i>vald</i>)"
                                    }
                                    +"&nbsp;&nbsp;$textToShow"
                                }
                            }
                        }
                    ),
                    ColumnDefinition(
                        widthGrow = 2,
                        title = "Antal anrop",
                        hozAlign = Align.RIGHT,
                        field = callsField
                    )
                ),
                rowSelected = { row ->
                    val item = row.getData().unsafeCast<SInfoRecord>()
                    if (item.calls > -1) HippoManager.itemSelectDeselect(item.itemId, item.itemType)
                },
                // todo: Hide the tabulator footer here
            )
        )
    }
}
