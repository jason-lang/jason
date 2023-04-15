require 'pygments'

# use a custom Pygments installation (directory that contains pygmentize)
Pygments.start ENV["PYGMENTS_PATH"] + '/..'

# example of registering a missing or additional lexer
Pygments::Lexer.create name: 'JasonAgent', aliases: ['jasonagent'],
    filenames: ['*.asl'], mimetypes: ['text/x-jason']

Pygments::Lexer.create name: 'JasonProject', aliases: ['jasonproject'],
    filenames: ['*.mas2j'], mimetypes: ['text/x-jasonproject']

Pygments::Lexer.create name: 'JaCaMoProject', aliases: ['jacamoproject'],
    filenames: ['*.jcm'], mimetypes: ['text/x-jacamoproject']
