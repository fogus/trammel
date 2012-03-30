require 'watchr'

watch('test/(.*)\.clj') {|t| system "lein test"}
watch('src/(.*)\.clj')  {|t| system "lein test"}
watch('src/(.*)\.clj')  {|t| system "lein marg"}
