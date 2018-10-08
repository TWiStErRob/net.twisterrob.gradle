<?xml version="1.0" encoding="utf-8"?>
<!--suppress CheckValidXmlInScriptTagBody -->
<xsl:stylesheet
	xmlns:xml="http://www.w3.org/XML/1998/namespace"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	exclude-result-prefixes="xml xsi"
	version="1.0"
>

	<xsl:output method="html" encoding="utf-8" indent="yes" omit-xml-declaration="yes" />

	<xsl:template match="/violations">
		<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
		<html>
			<head>
				<!--<meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> inserted by <xsl:output />-->
				<title>
					<xsl:value-of select="@project" />
					— Violations Report
				</title>
				<xsl:copy-of select="$style" />
				<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.3.1/jquery.slim.min.js" />
				<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.12.0/highlight.min.js" />
				<link type="text/css" rel="stylesheet"
				      href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.12.0/styles/default.min.css" />
				<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.12.0/languages/gradle.min.js" />
				<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.12.0/languages/java.min.js" />
				<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.12.0/languages/kotlin.min.js" />
				<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.12.0/languages/xml.min.js" />
				<script src="https://cdnjs.cloudflare.com/ajax/libs/markdown-it/8.4.2/markdown-it.min.js" />
				<xsl:copy-of select="$script" />
			</head>
			<body>
				<h1>Violations report for
					<xsl:value-of select="@project" />
				</h1>

				<div id="toc">
					<xsl:call-template name="toc" />
				</div>
				<div id="categories">
					<xsl:apply-templates select="category" />
				</div>
			</body>
		</html>
	</xsl:template>

	<xsl:template name="toc">
		<xsl:variable name="id" select="'toc'" />
		<h2 id="{$id}">Summary</h2>
		<ul id="{$id}-categories">
			<xsl:apply-templates select="category" mode="toc" />
		</ul>
	</xsl:template>

	<xsl:template match="category" mode="toc">
		<xsl:variable name="id" select="@name" />
		<li data-id="toc-{$id}">
			<span class="toc-count">
				<xsl:value-of select="count(.//violation)" />
			</span>
			×
			<a href="#{$id}">
				<xsl:value-of select="@name" />
			</a>
			<ul id="{$id}-reporters">
				<xsl:apply-templates select="reporter" mode="toc" />
			</ul>
		</li>
	</xsl:template>

	<xsl:template match="reporter" mode="toc">
		<xsl:variable name="id" select="concat(../@name, '-', @name)" />
		<li data-id="toc-{$id}">
			<span class="toc-count">
				<xsl:value-of select="count(.//violation)" />
			</span>
			×
			<a href="#{$id}">
				<xsl:value-of select="@name" />
			</a>
			<ul id="{$id}-rules">
				<xsl:call-template name="toc-rules" />
			</ul>
		</li>
	</xsl:template>

	<xsl:template name="toc-rules">
		<xsl:for-each select=".//violation[not(details/@rule = preceding-sibling::violation/details/@rule)]">
			<xsl:variable name="rule" select="details/@rule" />
			<xsl:variable name="sameRuleViolations" select="../violation[details/@rule = $rule]" />
			<xsl:variable name="id" select="concat(details/@category, '-', source/@reporter, '-', $rule)" />
			<li data-id="toc-{$id}">
				<span class="toc-count">
					<xsl:value-of select="count($sameRuleViolations)" />
				</span>
				×
				<a href="#{$id}">
					<code class="rule">
						<xsl:value-of select="$rule" />
					</code>
				</a>
				(
				<xsl:for-each select="$sameRuleViolations[
							not(location/@module = preceding-sibling::violation[
								details/@rule = $rule
							]
							/location/@module)
						]
						/location/@module">
					<xsl:sort />
					<code class="module">
						<xsl:value-of select="." />
					</code>
					<xsl:if test="position() != last()">,</xsl:if>
				</xsl:for-each>
				)
			</li>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="category">
		<h2 id="{@name}">
			<xsl:value-of select="@name" />
		</h2>
		<ul class="reporters">
			<xsl:apply-templates select="reporter" />
		</ul>
	</xsl:template>

	<xsl:template match="reporter">
		<h3 id="{../@name}-{@name}">
			<xsl:value-of select="@name" />
		</h3>
		<ul class="violations">
			<xsl:apply-templates select="violation" />
		</ul>
	</xsl:template>

	<xsl:template match="violation">
		<xsl:if test="details/@rule != concat(preceding-sibling::violation[1]/details/@rule, '')">
			<!-- concat in test makes sure the first violation (which has no preceding nodes)
				 is also `true` and not `no node` coerced to `false`) -->
			<a name="{details/@category}-{source/@reporter}-{details/@rule}" />
		</xsl:if>
		<div class="violation" xml:space="preserve">
			<b><code class="rule copyable" title="To suppress:&#xA;&#xA;{details/@suppress}&#xA;&#xA;Click to copy!" onClick="copyToClipboard(`{details/@suppress}`)"><xsl:value-of select="details/@rule" /></code>: <xsl:value-of select="details/title" /></b><br />
			<details class="location" open="open">
				<xsl:if test="details/context/@type = 'code'">
					<!--<xsl:attribute name="open">open</xsl:attribute>-->
				</xsl:if>
				<summary>
					<code class="module"><xsl:value-of select="location/@modulePrefix" />:<b><xsl:value-of select="location/@moduleName" /></b></code><!--
					--><xsl:if test="location/@variant != '*'">(<xsl:value-of select="location/@variant" />)</xsl:if>:
					<a class="file" href="{location/@fileAbsoluteAsUrl}">
						<xsl:value-of select="location/@pathRelativeToModule" /><b><xsl:value-of select="location/@fileName" /></b><!--
						--><xsl:if test="location/@startLine != '0'"><!--
							-->:<xsl:value-of select="location/@startLine" /><!--
							--><xsl:if test="location/@startLine != location/@endLine">-<xsl:value-of select="location/@endLine" /></xsl:if>
						</xsl:if>
					</a>
				</summary>
				<xsl:if test="details/context/@type = 'code'">
					<pre class="hljs"><code><!--
						--><script>
							render.code(
								`<xsl:value-of select="details/context" />`,
								{
									language: '<xsl:value-of select="details/context/@language" />',
									firstLine: <xsl:value-of select="details/context/@startLine" />,
									highlightStart: <xsl:value-of select="location/@startLine" />,
									highlightEnd: <xsl:value-of select="location/@endLine" />
								}
							)
						</script><!--
					--></code></pre>
				</xsl:if>
				<xsl:if test="details/context/@type = 'archive'">
					<pre><code><xsl:value-of select="details/context" /></code></pre>
				</xsl:if>
				<xsl:if test="details/context/@type = 'image'">
					<p><img src="{details/context/text()}" /></p>
				</xsl:if>
			</details>
			<details class="description">
				<summary>
					<script>render.markdown(`<xsl:value-of select="details/message" />`)</script>
				</summary>
				<section>
					<script>render.markdown(`<xsl:value-of select="details/description" />`)</script>
				</section>
			</details>
		</div>
	</xsl:template>

	<xsl:variable name="script" xml:space="preserve">
	<!--suppress JSUnusedLocalSymbols -->
	<script><![CDATA[/*&lt;![CDATA[*/
	var md = window.markdownit({
		highlight: function (str, lang) {
			if (lang && hljs.getLanguage(lang)) {
				try {
					return hljs.highlight(lang, str).value;
				} catch (__) {
				}
			}
			return ''; // use external default escaping
		}
	});

	function copyToClipboard(text) {
		var $temp = $("<textarea>");
		$("body").append($temp);
		$temp.val(text).select();
		document.execCommand("copy");
		$temp.remove();
	}

	var render = {
		markdown: function (text) {
			var html = md.render(text);
			document.write(html);
		},
		code: function (text, options) {
			options = options || {};
			var lang = options.language || 'text';
			var html = hljs.highlight(lang, text).value;
			html = addSourceLineNumbers(
				html,
				options.firstLine || 1,
				options.highlightStart || -1,
				options.highlightEnd || -1
			);
			document.write(html);
		}
	};

	// based on http://bellido.us/blog/07-06-2014-Adding-LineNumbers-highlight-js.html
	function addSourceLineNumbers(html, start, highlightStart, highlightEnd) {
		var prefix = 'prefix';
		var current = start;

		function line(number) {
			var classes = 'line';
			if (highlightStart === number) classes += ' start';
			if (highlightEnd === number) classes += ' end';
			if (highlightStart <= number && number <= highlightEnd) classes += ' highlight';
			return '<a class="' + classes + '" name="' + prefix + number + '">' + number + '</a>'
		}

		var result = html.replace(/\n/g, function () {
			return "\n" + line(++current);
		});

		return line(start) + result;
	}

	/*]]&gt;*/]]></script>
	</xsl:variable>
	<xsl:variable name="style" xml:space="preserve">
	<style type="text/css"><![CDATA[/*&lt;![CDATA[*/
	body {
		background-color: #f5f5f5;
		font-family: sans-serif;
	}

	code, pre {
		padding: 0;
		margin: 0;
	}

	a {
		color: rgb(83, 109, 254);
		text-decoration: none;
	}

	a:focus {
		text-decoration: underline;
	}

	a:active {
		color: rgb(83, 109, 254);
		text-decoration: underline;
	}

	a.file {
		font-size: smaller;
	}

	.violation {
		padding: 8px;
		margin-bottom: 8px;
		background: #fff;
		border-radius: 2px;
		box-shadow: 0 2px 2px 0 rgba(0, 0, 0, .14), 0 3px 1px -2px rgba(0, 0, 0, .2), 0 1px 5px 0 rgba(0, 0, 0, .12);
	}

	.violation > details.description > section {
		max-width: 900px;
	}

	.violation > details.description > summary > p {
		display: inline;
	}

	code.rule.copyable {
		cursor: pointer;
	}

	details:first-of-type {
		margin-top: 4px;
	}

	details + details {
		margin-top: 8px;
	}

	details > summary:hover {
		cursor: pointer;
	}

	details > summary:focus {
		outline: 0;
	}

	details > summary + * {
		margin-left: 16px;
		border-left: 4px solid #ddd;
		padding-left: 8px;
	}

	ul {
		/* match start padding to balance backgrounds */
		padding-inline-end: 40px;
	}

	.hljs {
		/* space for .hljs .line */
		padding-left: 40px !important;
		position: relative;
	}

	.hljs .line {
		display: inline-block;
		position: absolute;
		left: 0;
		width: 30px;
		text-align: right;
	}

	.hljs .line.highlight {
		background: #ffdddd;
		font-weight: bold;
		color: red;
	}

	/*]]&gt;*/]]></style>
	</xsl:variable>

</xsl:stylesheet>
