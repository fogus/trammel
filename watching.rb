require 'watchr'

watch('test/(.*)\.clj') {|t| system "lein multi test"}
watch('src/(.*)\.clj')  {|t| system "lein multi test"}
watch('src/(.*)\.clj')  {|t| system "lein marg"}
