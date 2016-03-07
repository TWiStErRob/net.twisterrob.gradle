// http://bl.ocks.org/sdjacobs/3900867adc06c7680d48
d3.dijkstra = function () {
	var dijkstra = {},
			nodes, edges, source,
			distanceFun = function () {
				return 1;
			},
			outEdges = function (d) {
				return d.links;
			},
			dispatch = d3.dispatch('start', 'step', 'end');

	dijkstra.run = function (src) {
		source = src;
		var unvisited = [];

		nodes.forEach(function (d) {
			if (d != src) {
				d.distance = Infinity;
				unvisited.push(d);
				d.visited = false;
				d.path = null;
			}
		});

		var current = src;
		current.distance = 0;

		function step() {
			current.visited = true;
			outEdges(current).forEach(function (link) {
				var tar = link.target;
				if (!tar.visited) {
					var dist = current.distance + distanceFun(link);
					if (dist < tar.distance) {
						tar.distance = dist;
						tar.path = link;
					}
				}
			});
			if (unvisited.length == 0 || current.distance == Infinity) {
				dispatch.end()
				return true;
			}
			unvisited.sort(function (a, b) {
				return b.distance - a.distance
			});

			current = unvisited.pop()

			dispatch.step();

			return false;
		}

		d3.timer(step);
	};

	dijkstra.distance = function (_) {
		if (!arguments.length) {
			return distanceFun;
		} else {
			distanceFun = typeof _ === 'function' ? _ : function () {
				return _;
			};
			return dijkstra;
		}
	};

	dijkstra.out = function (_) {
		if (!arguments.length) {
			return outEdges;
		} else {
			outEdges = typeof _ === 'function' ? _ : function (d) {
				return d[_];
			};
			return dijkstra;
		}
	};

	dijkstra.nodes = function (_) {
		if (!arguments.length) {
			return nodes;
		} else {
			nodes = _;
			return dijkstra;
		}
	};

	dijkstra.edges = function (_) {
		if (!arguments.length) {
			return edges;
		} else {
			edges = _;
			return dijkstra;
		}
	};

	dijkstra.source = function (_) {
		if (!arguments.length) {
			return source;
		} else {
			source = _;
			return dijkstra;
		}
	};


	dispatch.on('start.code', dijkstra.run);

	return d3.rebind(dijkstra, dispatch, 'on', 'end', 'start', 'step');
};

/*
 var dijkstra = d3.dijkstra()
 .nodes(nodes)
 .edges(links)
 .out('revLinks')
 ;

 var color = d3.scale.linear()
 .domain([0, 5, 10])
 .range(['green', 'yellow', 'red'])
 ;

 dijkstra.on('step', function() {
 link.classed('path', false);
 node.each(function(d) {
 if (d.path != null) {
 d3.select(d.path.link.ui).classed('path', true);
 }
 });
 });

 dijkstra.on('end', function() {
 var id = dijkstra.source().id;
 window.title = id;
 });


 whereever the starting point is: dijkstra.start(d);
 */
