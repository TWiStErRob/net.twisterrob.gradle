var algo = function() {
	return {
		transitiveReduction: transitiveReduction
	};

	/**
	 * var graph = { node1: ['node2'], node2: ['node1'], node3: ['node1', 'node2']};
	 * transitiveReduction(graph); // in place operation
	 * @see http://stackoverflow.com/a/11237184/253468
	 */
	function transitiveReduction(graph) {
		function children(name) {
			return graph[name];
		}
		for (var vertex0 in graph) { // for vertex0 in vertices
			var done = {}; // we'll run a DFS for each node
			depthFirstSearch(children(vertex0), vertex0, vertex0, done, []);
		}

		/**
		 * Runs a DFS on graph starting from vertex0
		 * @param vertex0:String root of DFS search
		 * @param child0:String current node during search
		 * @param done:Map<String, boolean> nodes that are done
		 * @param path:Array<String> list of nodes to get from vertex0 to child0
		 */
		function depthFirstSearch(edges, vertex0, child0, done, path) {
			if (done[child0]) return; // if child0 in done

			path.push(child0); // path.pushLast(child0)
			var children0 = children(child0);
			for (var childIndex in children0) { var child = children0[childIndex]; // for child in child0.children
				if(child0 != vertex0) {
					var edgeIndex = edges.indexOf(child); 
					if (edgeIndex !== -1) {
						edges.splice(edgeIndex, 1); // edges.discard((vertex0, child))
						path.push(child);
						//console.log("Removing edge " + vertex0 + " -> " + child + " because it duplicates " + path);
						path.splice(-1, 1);
					}
				}
				depthFirstSearch(edges, vertex0, child, done, path); // depthFirstSearch(edges, vertex0, child, done)
			}
			done[child0] = true; // done.add(child0)
			path.splice(-1, 1); // path.popLast(child0)
		}
	};
}();
