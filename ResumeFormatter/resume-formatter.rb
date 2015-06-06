require_relative 'resume-writer'

class ResumeFormatter

  def self.fromSource(data)
    return ResumeFormatter.new(data)
  end

  def formatWith(template_file)
    return ResumeWriter.new template_file, filteredData()
  end

  def reject(&section_filter)
    @section_exclude_filters << section_filter
    return self
  end

  def initialize(data)
    @data = data
    @section_exclude_filters = Array.new
  end

  private

  def filteredData()
    filtered_data = @data.reject{|section_name, section_value|
      rejectSection? section_name
    }
    return ResumeFormatter.markFirstAndLast filtered_data
  end

  def rejectSection?(section_name)
    @section_exclude_filters.each {|filter|
      return true if filter.call(section_name)
    }
    return false
  end

  def self.markFirstAndLast(obj)
    if obj.is_a?(Array)
      first, last = 0, obj.size - 1
      obj = obj.each_with_index.map do |e,i|
        if e.is_a?(Hash) and e.key?('row')
          e['first'] = true if i == first
          e['last']  = true if i == last
        else
          e = self.markFirstAndLast e
        end
        e
      end
    elsif obj.is_a?(Hash)
      obj = Hash[obj.map {|k,v| [k, self.markFirstAndLast(v)]}]
    end
    return obj
  end
end
