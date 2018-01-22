buildscript {
	repositories {
		jcenter()
	}

	dependencies {
		classpath(kotlin("gradle-plugin"))
//		classpath(kotlin("sam-with-receiver"))
	}
}

plugins {
	`groovy`
	`kotlin-dsl`
}

apply {
	plugin("kotlin")
//	plugin("kotlin-sam-with-receiver")
}

repositories {
	jcenter()
}

dependencies {
	implementation(kotlin("gradle-plugin"))
}
