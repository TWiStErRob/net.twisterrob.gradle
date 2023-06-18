'use strict';

d3.select(document).on("DOMContentLoaded", function(event) {
	main();
});
d3.select("#menu-freeze").on('click', function menuFreeze_onClick(event) {
	force.stop();
});
d3.select("#menu-thaw").on('click', function menuThaw_onClick(event) {
	force.restart();
});
d3.select("#menu-fit").on('click', function menuFit_onClick(event) {
	force.stop();
	zoomFit(0.95, 500);
});

function main() {
	// needs extra dispatch because LoadWorker.SUCCEEDED is after window.onload
	setTimeout(function startUp() {
		console.debug(`Startup ${navigator.userAgent}`);
		buildLegend(legendData);
		details.init();
		// See net.twisterrob.gradle.graph.vis.d3.GraphWindow.setupBrowser
		// noinspection JSUnresolvedReference
		if (window.isJavaHosted === true) {
			console.debug("We're in JavaFX");
		} else {
			console.debug("We're in a browser");
			demo();
		}
	}, 0);
}

const zoom = d3
	.zoom()
	//.scaleExtent([ 1 / 4, 4 ])
	.on('zoom', function svgZoom(event) {
		const transform = event.transform;
		console.debug("zoom", transform);
		root.attr('transform', `translate(${transform.x},${transform.y}) scale(${transform.k})`);
	})
;

const svg = d3
	.select('svg')
	.call(zoom)
	//.on('mousemove', function svgMouseMove(event) {
	//	cursor.attr('transform', `translate(${d3.pointer(event, this)})`);
	//})
;

let demoIdCounter = 0;
svg.select('#background')
	.on('click', function backgroundClick() {
		selectNode(null);
	})
	.on('mousedown', function backgroundMiddleMouseDown(event) {
		if (event.button !== 1) {
			return;
		}
		event.preventDefault();
		const point = d3.pointer(event, node_group.node());
		model.add({
			id: `____${demoIdCounter++}____`,
			type: 'unknown',
			x: point[0],
			y: point[1],
		});
		/*
		// Add links to any nearby nodes.
		nodes.forEach(target => {
			const x = target.x - node.x,
			      y = target.y - node.y;
			if (Math.sqrt(x * x + y * y) < 30) {
				links.push({ source: node, target: target });
			}
		});
		*/
	})
;
//const cursor = svg.select('#cursor');
const root = svg.select('#root');
const node_group = svg.select('#nodes');
const link_group = svg.select('#links');

const force = d3
	.forceSimulation()
	.force('forceLink', d3.forceLink()
			.id(node => node.id)
			.distance(100) // "Average" width of nodes.
			.strength(0.4) // Don't pull too hard to allow layout forming.
	)
	.force('forceCharge', d3.forceManyBody()
			.strength(-400)
			.distanceMax(2000) // Reduce range of repel to constrain fly-away unconnected nodes.
	)
	// forceCenter() does nothing, so using x + y to pull things in.
	// .force('forceCenter', d3.forceCenter().strength(1))
	// Assuming a normal screen, so the nodes are laid out in a laying rectangle.
	.force("forceX", d3.forceX()
			.strength(0.05)
	)
	.force("forceY", d3.forceY()
			.strength(0.1)
	)
	// TODO when collision is enabled, the link strength can be increased a bit.
	//.force('forceCollision', d3.forceCollide(100))
	//.force('forceCollision', phys.forceCollideRect())
;

const details = function Details() {
	const det = d3.select('#details');
	const progressUI = det.select('#detail-running');
	const nameUI = det.select('#detail-name').insert('span');
	const projectUI = det.select('#detail-project');
	const taskUI = det.select('#detail-task');
	const typeUI = det.select('#detail-type');
	const stateUI = det.select('#detail-state');
	const posUI = det.select('#detail-pos');
	const dependsUI = det.select('#detail-depends');
	const dependentsUI = det.select('#detail-dependents');

	nameUI.on('click', function nameUIClick() {
		det.classed('collapsed', !det.classed('collapsed'));
	});
	/** @type {VisualTask|null} */
	let currentNode = null;
	let currentNodeIsLocked = false;
	let currentNodeIsActive = false;


	/**
	 * @param ui `d3.selection<ul>`, but don't know how to express it in JSDoc.
	 * @param {LogicalTaskId[]} deps
	 */
	function bindDeps(ui, deps) {
		ui
			.selectAll('li')
			.data(deps, d => d)
			.join('li')
			.text(d => d)
			.on('click', function detailsDependencyClick(event, d) {
				selectNode(graph[d]);
			})
		;
		ui
			.selectAll('span')
			.data(deps.length === 0 ? [ "nothing" ] : [ ])
			.join('span')
			.text(d => d)
		;
	}

	/**
	 * @param {VisualTask|null} d
	 */
	function display(d) {
		if (currentNode && !d && stateUI.text() === 'executing') {
			display(currentNode);
			return;
		}

		det.style('visibility', d? 'visible' : 'hidden');

		if (d) {
			nameUI.text(d.ui.label());
			autoSizeText(nameUI.node());
			projectUI.text(d.ui.project() || "root project");
			taskUI.text(d.ui.taskName());
			typeUI.text(d.type || 'normal');
			stateUI.text(d.state || 'scheduled');
			progressUI.style('display', d.state === 'executing'? 'block' : 'none');
			posUI.text(`${d.x.toFixed(2)}, ${d.y.toFixed(2)}`);

			bindDeps(dependsUI, d.deps);
			bindDeps(dependentsUI, d.depsInverse);
		}

		currentNode = d;
	}

	function autoPick() {
		const executing = nodes.filter(d => d.state === 'executing');
		return executing.length === 0? null : executing[0];
	}

	return {
		init() {
			display(null);
		},
		showNode(d) {
			if (!currentNodeIsLocked) {
				currentNodeIsActive = true;
				display(d);
			}
		},
		hideNode(/*d*/) {
			currentNodeIsActive = false;
			this.refreshDisplay();
		},
		lockNode(d) {
			if (d) {
				currentNodeIsLocked = true;
				display(d);
			} else {
				currentNodeIsLocked = false;
				display(autoPick());
			}
		},
		refreshDisplay() {
			if (!currentNodeIsLocked && !currentNodeIsActive) {
				display(autoPick());
			} else {
				display(currentNode);
			}
		},
	};
}();

/** @type {Object.<string, VisualTask>} */
// noinspection ES6ConvertVarToLetConst
var graph = {}; // window.graph is referenced as such.
/** @type {VisualTask[]} */
const nodes = force.nodes();
/** @type {VisualDep[]} */
const links = force.force('forceLink').links();

function rebuild() {
	const uiLinks = link_group
		.selectAll('.link')
		.data(links, /** @param {VisualDep} d */ d => d.linkId())
		.join(
			enter => enter
				.append('line')
				.each(/** @param {VisualDep} d */ function storeUiLink(d) { d.ui.edge = this; })
				.attr('id', /** @param {VisualDep} d */ d => d.linkId())
				.attr('class', 'link')
				.style('marker-start', 'url(#arrow-in)')
				//.style('marker-end', 'url(#arrow-out)')
		)
	;

	const uiNodes = node_group
		.selectAll('.node')
		.data(nodes, d => d.ui.nodeId())
		.joinNodes()
		.on('click', function nodeClick(event, d) {
			selectNode(d);
		})
		.on('mousedown', function nodeMouseDown(event, d) {
			details.lockNode(d);
		})
		.on('mouseup', function nodeMouseUp(/*event, d*/) {
			details.lockNode(null);
		})
		.call(d3
			.drag()
			.on('start', function dragStart(event, d) {
				if (!event.active) {
					force.alphaTarget(0.3).restart();
				}
				d.fx = d.x;
				d.fy = d.y;
			})
			.on('drag', function dragMove(event, d) {
				d.fx = event.x;
				d.fy = event.y;
			})
			.on('end', function dragEnd(event, d) {
				if (!event.active) {
					force.alphaTarget(0);
				}
				d.fx = null;
				d.fy = null;
			})
		)
	;

	details.refreshDisplay();

	force.stop()
	force.nodes(nodes);
	force.force('forceLink').links(links);
	force.on('tick', function forceTick() {
		//const q = d3.geom.quadtree(nodes);
		//node.each(function(n) { q.visit(phys.collide(n)) });
		uiLinks
			.attr('x1', /** @param {VisualDep} d */ d => phys.pointOnRect(d.source, d.target).x)
			.attr('y1', /** @param {VisualDep} d */ d => phys.pointOnRect(d.source, d.target).y)
			.attr('x2', /** @param {VisualDep} d */ d => phys.pointOnRect(d.target, d.source).x)
			.attr('y2', /** @param {VisualDep} d */ d => phys.pointOnRect(d.target, d.source).y)
		;

		uiNodes
			.attr('transform', d => `translate(${d.x},${d.y})`)
		;
	});
	force.restart();
}

function selectNode(d) { // TODO use #task-id for back navigation support
	node_group.selectAll('.node.selected').classed('selected', false);
	if (d) {
		if (d.ui) {
			d3.select(d.ui.node).classed('selected', true);
			details.lockNode(d);
		} else {
			console.warn("Cannot select node, probably not displayed.", d);
		}
	} else {
		details.lockNode(null);
	}
}

/**
 * @param {number} paddingPercent 0.0 to 1.0, recommended close to 1.0
 * @param {number} transitionDuration milliseconds, positive, can be 0
 */
function zoomFit(paddingPercent, transitionDuration) {
	const bounds = root.node().getBBox();
	const parent = root.node().parentElement;
	const fullWidth  = parent.clientWidth,
	      fullHeight = parent.clientHeight;
	const width  = bounds.width,
	      height = bounds.height;
	const midX = bounds.x + width / 2,
	      midY = bounds.y + height / 2;
	if (width === 0 || height === 0) {
		return; // Nothing to fit.
	}
	const scale = paddingPercent / Math.max(width / fullWidth, height / fullHeight);
	const translate = [ fullWidth / 2 - scale * midX, fullHeight / 2 - scale * midY ];

	console.debug("zoomFit", translate, scale);
	svg
		.transition()
		.duration(transitionDuration)
		.call(zoom.transform, d3.zoomIdentity.translate(translate[0], translate[1]).scale(scale));
}

// noinspection JSValidateJSDoc
/**
 * @see net.twisterrob.gradle.graph.vis.d3.interop.TaskTypeSerializer for type values.
 * @see net.twisterrob.gradle.graph.vis.d3.interop.TaskResultSerializer for state values.
 */
const legendData = [
	{ type: 'normal', title: "task" }, // Normally this is empty string, add actual value to make it visible.
	{ state: 'success', title: "SUCCESS" },
	{ state: 'uptodate', title: "UP-TO-DATE" },
	{ state: 'failure', title: "FAILED" },
	{ state: 'skipped', title: "SKIPPED" },
	{ state: 'fromcache', title: "FROM-CACHE" },
	{ state: 'nosource', title: "NO-SOURCE" },
	{ state: 'executing', title: "Executing..." },
	{ type: 'requested', title: "Requested" },
	{ type: 'excluded', title: "--exclude-task" },
	{ state: 'nowork', title: "No Work" },
	{ type: 'unknown', title: "Unknown" },
	{ state: 'selected', title: "Selected" }, // Special state, only in UI.
];
function buildLegend(legendData) {
	for (const i in legendData) {
		const legendDatum = legendData[i];
		// TODO use nodify?
		legendDatum.ui = {
			data: legendDatum,
			project() { return ":legend"; },
			taskName() { return "legendTask"; },
			label() { return this.data.title; },
			nodeId() { return `legend_${this.data.id}`; },
		};
		legendDatum.deps = [];
		legendDatum.depsInverse = [];
		legendDatum.id = legendData[i].type || legendData[i].state;
	}

	const legend = svg
		.select('#legend');

	const legendHeader = legend
		.select('#legend-header')
		.on('click', toggleLegend)
	;
	const headerHeight = legendHeader.node().getBBox().height;

	// noinspection JSUnusedLocalSymbols
	const legendNodes = legend
		.selectAll('.node')
		.data(legendData, d => d.ui.nodeId())
		.joinNodes()
		.each((d, i) => {
			d.x = 0;
			d.y = headerHeight + 10 + i * d.height * 1.3;
		})
		.attr('transform', d => `translate(${d.x},${d.y})`)	;

	const legendBackground = legend
		.select('#legend-bg')
		.on('click', toggleLegend)
	;

	function toggleLegend() {
		legend.classed('collapsed', !legend.classed('collapsed'));
		autoSizeLegend();
	}

	function autoSizeLegend() {
		legendBackground.style('display', 'none');
		const legendBox = legend.node().getBBox();
		const legendPadding = 16;
		legendBackground
			.attr('x', legendBox.x - legendPadding)
			.attr('y', legendBox.y - legendPadding)
			.attr('width', legendBox.width + 2 * legendPadding)
			.attr('height', legendBox.height + 2 * legendPadding)
		;
		legendBackground.style('display', 'block');
	}

	autoSizeLegend();
	legend.style('visibility', 'visible');
}

/**
 * @param {VisualTask} d
 * @return {string}
 */
function nodeClasses(d) {
	return 'node'
		 + (d.type? ' ' + d.type : '')
		 + (d.state? ' ' + d.state : '')
		 + (d.deps.length === 0? ' leaf' : '')
		 + (d.deps.length === 1 && d.depsInverse.length === 1? ' straight' : '')
	;
}

// Note: this could almost be replaced with ….call(joinNodes).…,
// but call() doesn't return the function's return value, so it doesn't chain properly.
// noinspection JSPotentiallyInvalidConstructorUsage
d3.selection.prototype.joinNodes = function joinNodes() {
	return this
		.join(
			enter => {
				const uiNodes = enter
					.append('g')
					.each(function storeUiNode(d) { d.ui.node = this; })
					.attr('id', d => d.ui.nodeId())
					.on('mouseover', function nodeMouseOver(event, d) {
						details.showNode(d);
					})
					.on('mouseout', function nodeMouseOut(event, d) {
						details.hideNode(d);
					})

				const rect = uiNodes.append('rect')
					.each(function storeUiNodeBackground(d) { d.ui.bg = this; })
				;
				// This needs to be appended after the rectangle so that the DOM-order is correct for z-ordering.
				//noinspection JSUnusedLocalSymbols
				const text = uiNodes.append('text')
					.each(function storeUiNodeText(d) { d.ui.text = this; })
					.classed('label', true)
					.text(d => d.ui.label())
				;
				//uiNodes.append('circle').attr('cx', 0).attr('cy', 0).attr('r', 3).attr('fill', 'red');
				const padding = { x: 5, y: 4 };

				// This needs to be after inserting text, because the background rectangle size depends on the rendered text.
				rect
					.attr('width', d => d.ui.text.getBBox().width + 2.0 * padding.x)
					.attr('height', d => d.ui.text.getBBox().height + 2.0 * padding.y)
					.attr('x', d => +d3.select(d.ui.bg).attr('width') / -2.0)
					.attr('y', d => +d3.select(d.ui.bg).attr('height') / -2.0)
				;

				//noinspection JSUnusedLocalSymbols
				const module = uiNodes.append('text')
					.classed('project', true)
					.text(d => d.ui.project())
					.attr('x', d => +d3.select(d.ui.bg).attr('x') + +d3.select(d.ui.bg).attr('width') - padding.x)
					.attr('y', d => +d3.select(d.ui.bg).attr('y') - 1)
				;
				// This needs to be after everything is built, because it depends on the rendered size.
				return uiNodes
			},
		)
		// Recalculate the visual state on new and existing nodes, as the state might've changed.
		.attr('class', nodeClasses)
		.each(/** @param {VisualTask} d */ function resizeUiNode(d) {
			const box = d.ui.node.getBBox();
			d.width = box.width;
			d.height = box.height;
		})
	;
}

let model = function Model() {
	class VisualDep {
		/**
		 * @param {VisualTask} fromNode
		 * @param {VisualTask} toNode
		 */
		constructor(fromNode, toNode) {
			/** @type {VisualTask} */
			this.source = fromNode;
			/** @type {VisualTask} */
			this.target = toNode;
			/** @type {number} */
			this.weight = 1;
			/** @type {Object} */
			this.ui = {
				/** @type {SVGElement} */
				edge: null,
			};
		}

		/**
		 * @returns {VisualDepId}
		 */
		linkId() {
			return constructLinkId(this.source.id, this.target.id);
		}

		toString() {
			return `${this.source} -> ${this.target}`;
		}
	}

	return {
		/**
		 * @param {string|Object.<string, LogicalTask>} rawGraph
		 */
		init(rawGraph) {
			/**
			 * @param {LogicalTask} d
			 * @return {boolean}
			 */
			function filter(d) { return d.type === 'unknown'; }

			/**
			 * @type {Object.<string, VisualTask>} TODO not actually the right type yet when executing.
			 */
			const graph = (typeof rawGraph === 'string')
				? JSON.parse(rawGraph)
				: rawGraph;
			window.graph = graph;

			nodes.length = 0;
			for (const dataIndex in graph) {
				const data = graph[dataIndex];
				if (filter(data)) {
					continue;
				}
				const node = nodify(dataIndex, data);
				nodes.push(node);
			}
			// TODO After the previous loop, graph changes type from Map<string, LogicalTask> to Map<string, VisualTask>.
			links.length = 0;
			for (const nodeIndex in graph) {
				const fromNode = graph[nodeIndex];
				if (filter(fromNode)) {
					continue;
				}
				for (const depIndex in fromNode.deps) {
					const toNode = graph[fromNode.deps[depIndex]];
					if (filter(toNode)) {
						continue;
					}
					toNode.depsInverse.push(fromNode.id);
					const link = new VisualDep(fromNode, toNode);
					links.push(link);
					fromNode.links.push(link);
				}
			}
			rebuild();
			force
				.alphaTarget(1)
				.tick(300)
				.alphaTarget(0)
			;
			// setTimeout is required to allow the browser to render the nodes.
			setTimeout(() => zoomFit(0.95, 0), 0);
		},
		/**
		 * @param {LogicalTaskId} task
		 * @param {string} result
		 */
		update(task, result) {
			// This method should be just the commented lines, except it's too slow for an ever-changing execution.
			// Gradle changes task states multiple times per second, so it's better to focus on updating what's really changed.
			//graph[task].state = result;
			const node = d3.select(`#${constructNodeId(task)}`);
			const data = node.datum();
			data.state = result;
			//rebuild();
			node.attr("class", nodeClasses(data));
			details.refreshDisplay();
		},
		/**
		 * @param {LogicalTask} data
		 */
		add(data) {
			nodes.push(nodify(data.id, data));
			rebuild();
		},
	};

	/**
	 * @param {LogicalTaskId} name
	 * @return {string}
	 */
	function cleanName(name) {
		// Normally this would clean `:foo:bar` to `-foo-bar`, but generalized to be safe.
		return name.replace(/[^a-zA-Z0-9_]/g, '-');
	}

	/**
	 * @param {LogicalTaskId} name
	 * @return {VisualTaskId}
	 */
	function constructNodeId(name) {
		return `node_${cleanName(name)}`;
	}

	/**
	 * @param {LogicalTaskId} from
	 * @param {LogicalTaskId} to
	 * @return {VisualDepId}
	 */
	function constructLinkId(from, to) {
		return `link_${cleanName(from)}_${cleanName(to)}`;
	}

	/** @typedef {string} LogicalTaskId */
	/** @typedef {string} VisualTaskId */
	/** @typedef {string} VisualDepId */

	/**
	 * @typedef {Object} LogicalTask
	 * @property {LogicalTaskId} id
	 * @property {string} label
	 * @property {string} type
	 * @property {string} state
	 */

	/**
	 * @typedef {Object} VisualTask
	 * @augments LogicalTask TODO remove this, it's weird that objects change type in place.
	 * @property {LogicalTaskId} id from LogicalTask
	 * @property {string} label from LogicalTask
	 * @property {string} type from LogicalTask
	 * @property {string} state from LogicalTask
	 * @property {LogicalTaskId[]} deps
	 * @property {LogicalTaskId[]} depsInverse
	 * @property {VisualDep[]} links
	 * @property {Object} ui
	 * @property {LogicalTask} ui.data
	 * @property {SVGGElement} ui.node
	 * @property {SVGTextElement} ui.text
	 * @property {SVGRectElement} ui.bg
	 * @property {function(): string} ui.project
	 * @property {function(): string} ui.taskName
	 * @property {function(): string} ui.label
	 * @property {function(): VisualTaskId} ui.nodeId
	 * @property {number} x added by force
	 * @property {number} y added by force
	 * @property {number} width
	 * @property {number} height
	 * @property {function(): number} x2
	 * @property {function(): number} y2
	 * @property {function(): string} toString
	 */

	/**
	 * @param {LogicalTaskId} id
	 * @param {LogicalTask} node
	 * @returns {VisualTask}
	 */
	function nodify(id, node) {
		const defaults = {
			id: id,
			deps: [],
			depsInverse: [],
		};
		const viewModel = {
			links: [],
			ui: {
				data: node,
				node: null,
				text: null,
				bg: null,
				project() {
					const label = this.data.label || this.data.id;
					return label.replace(/^:?(.+):.+$|.*/, '$1');
				},
				taskName() {
					const label = this.data.label || this.data.id;
					return label.replace(/^:?(.*):/, '');
				},
				label() {
					const label = this.taskName();
					if (label.length <= 25) {
						return label;
					}
					const parts = label.split(/(.[a-z]+)/).filter(g => g)
					if (parts.length <= 2) {
						return label.substring(0, 12) + '…' + label.substring(label.length - 12);
					}
					return parts[0] + '…' + parts[parts.length - 1];
				},
				nodeId() {
					return constructNodeId(this.data.id);
				},
			},
			x2() {
				return this.x + this.width;
			},
			y2() {
				return this.y + this.height;
			},
		};

		node.toString = function() {
			return `${this.id} @ ${this.x},${this.y} ${this.width}x${this.height}`;
		};

		for (const i in defaults) {
			if (node[i] === undefined) {
				node[i] = defaults[i];
			}
		}
		for (const i in viewModel) {
			if (node[i] === undefined) {
				node[i] = viewModel[i];
			} else {
				console.error(`Property ${i} already exists`, node, viewModel);
			}
		}
		return node; // == $.merge(node, defaults, viewModel);
	}
}();

/**
 * @param {SVGElement} elem
 * @param {number} min
 * @param {number} max
 * @param {number} step
 * @param {number} lastChange
 */
function autoSizeText(elem, min = 1, max = Infinity, step = 1, lastChange = 0) {
	/**
	 * @param {string} pixelStyle
	 * @returns {number}
	 */
	function px(pixelStyle) {
		return +pixelStyle.slice(0, -2);
	}

	const elemBounds  = elem.getBoundingClientRect(),
	      style       = window.getComputedStyle(elem),
	      parentStyle = window.getComputedStyle(elem.parentNode)
	;
	const elemSize = { width: elemBounds.width, height: elemBounds.height };
	const parentSize = { width: px(parentStyle.width), height: px(parentStyle.height) };
	let fontSize = px(style.fontSize);

	//console.debug(`autoSizeText(${elem.textContent}, ${min}, ${max})`, fontSize, elemSize, parentSize);
	let change = 0, stop = false;
	const needsSmaller = elemSize.width > parentSize.width || elemSize.height > parentSize.height;
	const needsBigger = elemSize.width < parentSize.width && elemSize.height < parentSize.height;
	const lastSmaller = lastChange <= 0;
	const lastBigger = lastChange >= 0;
	if (lastSmaller && needsSmaller) { //console.debug('lastSmaller needsSmaller')
		change = -step; // continue shrinking
	} else if (lastBigger && needsBigger) { //console.debug('lastBigger needsBigger')
		change = +step; // continue growing
	} else if (lastSmaller && needsBigger) { //console.debug('lastSmaller needsBigger')
		change = 0; // found the smallest fit
	} else if (lastBigger && needsSmaller) { //console.debug('lastBigger needsSmaller')
		change = -lastChange; // just overstepped the limit, backtrack once
		stop = true;
	} else if (!needsBigger && !needsSmaller) { //console.debug("just right");
		change = 0; // doesn't need to be any different
	} else {
		throw "Invalid state"
			+ ": lastSmaller=" + lastSmaller
			+ ", lastBigger=" + lastBigger
			+ ", needsSmaller=" + needsSmaller
			+ ", needsBigger=" + needsBigger
		;
	}

	if (change !== 0) {
		fontSize += change;
		if (min <= fontSize && fontSize <= max) {
			elem.style.setProperty('font-size', `${fontSize}px`);
		}
	} else {
		stop = true;
	}
	if (!stop) {
		autoSizeText(elem, min, max, step, change);
	}
}

/**
 * @returns {void}
 */
async function demo() {
	const graph = await d3.json("demos/com.android.application v1.2.0 - gradlew build.json");
	//const graph = await d3.json("demos/net.twisterrob.gradle - gradlew clean jar.json");
	model.init(graph);
	setTimeout(function demoDelayedEffects() {
		model.update(Object.keys(graph)[0], 'success');
		model.update(Object.keys(graph)[1], 'executing');
	}, 1000);
}
