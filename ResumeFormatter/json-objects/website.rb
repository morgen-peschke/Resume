require_relative 'entry'

class Website < Entry
  attr_accessor :label, :url, :short

  def initialize(hash)
    return unless hash.respond_to? :fetch

    @label = hash.fetch 'label', nil
    @url   = hash.fetch 'url',   nil

    if @url.respond_to? :partition
      @short = @url.partition(/https?:\/\/(?:www\.)?/)[-1]
    end
  end

end
