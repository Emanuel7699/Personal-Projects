package com.example.pipeline


class FlowSolver {
    fun edmondsKarp(edges: List<Edge>): Int {
        val sourceId = 0
        val sinkId = 1
        var totalFlow = 0

        val residualEdges = edges.map { it.copy() }.toMutableList()

        while (true) {
            val parentMap = mutableMapOf<Int, Int>()
            val edgeMap = mutableMapOf<Int, Edge>()
            val queue = ArrayDeque<Int>()

            queue.add(sourceId)
            parentMap[sourceId] = -1

            while (queue.isNotEmpty()) {
                val u = queue.removeFirst()
                if (u == sinkId) break

                residualEdges
                    .filter { it._from == u && it._capacity > it._flow }
                    .forEach { edge ->
                        if (!parentMap.containsKey(edge._to)) {
                            parentMap[edge._to] = u
                            edgeMap[edge._to] = edge
                            queue.add(edge._to)
                        }
                    }
            }

            if (!parentMap.containsKey(sinkId)) break

            var pathFlow = Int.MAX_VALUE
            var curr = sinkId
            while (curr != sourceId) {
                val edge = edgeMap[curr]!!
                pathFlow = minOf(pathFlow, edge._capacity - edge._flow)
                curr = parentMap[curr]!!
            }

            curr = sinkId
            while (curr != sourceId) {
                val edge = edgeMap[curr]!!
                edge._flow += pathFlow
                curr = parentMap[curr]!!
            }

            totalFlow += pathFlow
        }
        return totalFlow
    }

    fun calculatePercent(currentFlow: Int, maxCapacity: Int): Int {
        if (maxCapacity == 0) return 0
        return (currentFlow * 100) / maxCapacity
    }
}