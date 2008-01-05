# Created by IntelliJ IDEA.
# User: Roman.Chernyatchik
# Date: 27.12.2007
# Time: 21:37:42
# To change this template use File | Settings | File Templates.
ENV["idea.rake.debug.sources"] = "true"
ENV["idea.rake.debug.log"] = "true"

$: << File.expand_path(File.dirname(__FILE__) + "/..")

require "test/unit"
